---
layout: post
title: Use OpenAPI Instead of MCP for LLM Tools
tags: [machine learning, LLM, MCP, OpenAPI, gRPC]
index: ['/Computer Science/Machine Learning']
---

[Module Context Protocol (MCP)](https://modelcontextprotocol.io/introduction) is adopted by more and more people as a way to integrate tools to LLMs easily. However, I find it unintuitive and unnecessarily complex. So in this article, I'll explore how to use existing API servers as tools of LLMs instead of writing the functions in a completely new protocol. This can potentially become a simpler standard that only needs implementation an additional authentication flow (if authentication is needed).

The final result is in the repo [ai-tool-proto-experiment](https://github.com/wb14123/ai-tool-proto-experiment). It's a single file Scala script with less than 300 lines of code.
It doesn't use any LLM SDK, just simple HTTP calls to the LLM providers. It doesn't use any advance API either. Only chat completion API with structured output is needed.


## Goals and Non-Goals

Tool server is only part of MCP. It has more like prompts and resources. Personally I don't see much benefit to include so many use cases in a single protocol.

For example, prompts are just a server API that you can get all the pre-defined prompts. That's very easy to implement with any protocol and there is really no need to combine it with the LLM tool protocol.

So in this article, we will only explore how to integrate other services as tools of LLM, without caring about the other parts of MCP like prompts and resources.

There are also some other things that MCP doesn't resolve, like security. [This post](https://elenacross7.medium.com/%EF%B8%8F-the-s-in-mcp-stands-for-security-91407b33ed6b) summarized lots of security problems in MCP, and I don't think there is an easy way around that even if we use existing protocols like OpenAPI. So the goal in this experiment is only use trusted OpenAPI servers, without worrying about attacks like tool shadowing. With that said, authentication is still necessary to the OpenAPI server, which is a protect of the server instead of the client. MCP only added authentication into the spec recently. As you can see later, the authentication workflow I tried here is much more simpler and generic.

At last, use as little LLM API as possible is also a goal, so that it's easier to port the implementation to other LLM providers.

## The Implementation

Using something like OpenAPI is not a new idea. I've seen multiple people mentioned it on places like HackerNews. And during my implementation, I also found [Open WebUI](https://github.com/open-webui/open-webui), a tool I self hosted and used daily also added support to use OpenAPI servers as tools. Nevertheless, I still try to experiment my own implementation because I want to keep it as simple as possible, and also learn more details about the capability of such approach.

In the experiment, I tried both a simple [open source weather OpenAPI server](https://github.com/open-webui/openapi-servers/tree/main/servers/weather), and my own project [RSS Brain](https://www.rssbrain.com/). I'll try to explain how it is implemented and talk about an experiment result at the end.

## Define the Tool Calling Structure

Lots of LLM providers support tool calling APIs. We will avoid to use those APIs just to keep things simpler and make it more general available for other LLMs, including the self hosted ones. So instead, we define our own JSON schema that we want the LLM to follow and feed it as part of the system prompt, also use structured output API to enforce the LLM response follow the JSON schema. I said in the beginning that I want to use as little feature as possible, but I think structured output is an important enough feature I need to use in addition to the basic chat completion. Fortunately lots of other LLMs including the local ones like Ollama also support this feature.


Here are the response structure we want, in the format of Scala class definition:

```scala
case class ToolParam(
    httpRequestEndpoint: String,
    httpRequestPath: String,
    httpRequestHeaders: Option[Map[String, String]],
    httpRequestMethod: String,
    HttpPostBody: Option[String],
)

case class ChatResponse(
    callTool: Option[ToolParam] = None,
    toUser: Option[String] = None,
)
```

The LLM should either response to the user directly using `toUser` field, or ask the agent to call a HTTP API with `callTool` field. You can see the `ToolParam` definition is pretty generic: it can basically do any HTTP call.

For OpenAI, the structured output API only accept a subset of JSON schema definition. So instead of converting the structure to a JSON schema with a single line of Scala code, I need to manually write the OpenAI compatible one.

I also find OpenAI models, gpt-4o-last at least, often failed to generate response that meet the structure requirement even structured output is enabled. You still need to include the JSON schema into the system prompt to get the best chance.

Overall, here is the system prompt to let the system use the tools:

```scala
val systemPrompt: String = {
  val timeStr = ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
  s"""You are a helpful assistant.
     |
     |The current time is $timeStr.
     |
     |You have many tools to use by sending a http request to some API servers. Your response must be Json that
     |follows the Json schema definition:
     |
     |$chatResponseSchemaStr
     |
     |Either request a call to one of the APIs with `callTool` field, or
     |response to user directly with `toUser` field if there is no need to request to any tool or you need more
     |information from the user.
     |
     |Each tool has an optional authUrl that you can ask the user to open in the browser. If you get authentication
     | related errors when calling a tool, ask the user to open the authUrl in browser and copy the instruction back,
     | then use the instruction to try authentication again.
     |
     |Important:
     |
     |* Response only the JSON body. Never quote the response in something like json```...```.
     |* Never response to user directly without using the `toUser` field with a Json response.
     |* Only one of `callTool` and `toUser` field should be filled.
     |* Always include the `http` or `https` part for the `httpRequestEndpoint` field.
     |
     |""".stripMargin
}
```

You can see there are some extra points at the end, which are the cases I find the model hiccups very often.

## Feed the Tool Information Into LLM

Since OpenAPI can generate a structured document for the API server, either in JSON or YAML, we can feed the document directly into the LLM. In addition to the doc endpoint, we also need to provide the endpoint of the API servers, also an optional `authUrl` we will talk about later. Here is the definition of the tool in Scala classes, with the prompts:

```scala
case class ToolDef(
    httpEndpoint: String,
    openAPIPath: String,
    authUrl: Option[String] = None,
) {
  def prompt: String = {
    val authUrlPrompt = authUrl.map(url => s"Tool login URL: $url\n").getOrElse("")
    s"""----
       |Tool server endpoint: $httpEndpoint
       |
       |$authUrlPrompt
       |Tool's OpenAPI definition:
       |$openAPIDef
       |
       |----
       |
       |""".stripMargin
  }

  private def openAPIDef: String = {
    requests.get(httpEndpoint + openAPIPath).text()
  }
}
```

After system prompt, the tools prompt is sent as the first chat message to the LLM with a role of `developer`. I find it works better than put it into the system prompt, maybe because of the tool definition sometimes can be too long:

```scala
val tools = Seq(
  ToolDef(httpEndpoint = "https://grpc-gateway.rssbrain.com", openAPIPath = "/swagger.json",
    authUrl = Some("http://app.rssbrain.com/login?redirect_url=/llm_auth")),
)
val toolsPrompt = tools.map(_.prompt).mkString("\n")

val req = ChatRequest(
  messages = Seq(
    ChatMessage(role = "system", content = systemPrompt),
    ChatMessage(role = "developer", content =
      s"""
        |
        |Here are the OpenAPI definition of the tools:
        |
        |$toolsPrompt
        |
        |""".stripMargin),
  ),
)
loop(req, None, waitForUser = true)
```

## Authentication

As you can see from the system prompt above:

```
Each tool has an optional authUrl that you can ask the user to open in the browser. If you get authentication
related errors when calling a tool, ask the user to open the authUrl in browser and copy the instruction back,
then use the instruction to try authentication again.
```

We actually take advantage of the flexible of LLMs for our authentication flow: we define a `authUrl` for a tool server, which user can open in the browser. The URL will do necessary authentication flow, then return a nature language description about credentials and how to use it to do authentications with the APIs.

Ideally, the nature language instruction should be passed to the client in a secure way, for example, through callback to a local URL that the client serves. But for the simplicity of the experiment, I just ask the user the copy the instruction back to the conversation.

So here is what it looks like in the example below:

```
User input: Get all my RSS folders
Calling tool https://grpc-gateway.rssbrain.com//rss.FolderAPI/GetMyFolders ...
```

The user asks for the RSS folders, so LLM response with a `callTool` action. When trying to call the http API, it returns an error about authentication. We feed the result back to the LLM, then it responses to the user:

```
Assistant: It seems like your request for fetching RSS folders requires authentication. Please log in to your RSS Brain account and provide the token to proceed. You can open [this login page](http://app.rssbrain.com/login?redirect_url=/llm_auth) to login and obtain the necessary token.
```

You can see the LLM is asking the user to open a URL in the browser. When user open this URL in the browser, the service will prompt the user to login and redirect the user to a page with nature language instruction for LLM. The user copy the instruction back to the chat:

```
User input: Use `token` param in the APIs to do authentication. Your current token is `XXXXX`.
```

After added the user input to the LLM, LLM now knows how to fill in `callToo` params with necessary authentication info and the call finally succeed:

```
Calling tool https://grpc-gateway.rssbrain.com//rss.FolderAPI/GetMyFolders ...
Assistant: Here are your RSS folders:
...
```

The authentication flow makes it very flexible: the tool server can basically implement any kinds of authentication method as long as it provides a URL that contains the authentication instruction along credentials. You can even create a third party auth server if the original OpenAPI server doesn't provide this auth workflow, so that you can integrate any OpenAPI service with authentication.


## Results

Okay, everything is ready now. Let's try to use it. Follow the [readme](https://github.com/wb14123/ai-tool-proto-experiment) to install the dependencies and run the script.

Here is a real world example of using RSS Brain as one of the tool server:

```
-> ./ai-tool.sc
Compiling /home/wangbin/hobbi_source/ai-tool-proto-experiment/ai-tool.sc
User input: Hi
Assistant: Hello! How can I assist you today?
User input: Get all my RSS folders
Calling tool https://grpc-gateway.rssbrain.com//rss.FolderAPI/GetMyFolders ...
Assistant: It seems like your request for fetching RSS folders requires authentication. Please log in to your RSS Brain account and provide the token to proceed. You can open [this login page](http://app.rssbrain.com/login?redirect_url=/llm_auth) to login and obtain the necessary token.
User input: Use `token` param in the APIs to do authentication. Your current token is `XXXXX`.
Calling tool https://grpc-gateway.rssbrain.com//rss.FolderAPI/GetMyFolders ...
Assistant: Here are your RSS folders:

1. **Default Folder:**
   - Name: Default
   - Description: user default folder
   - Layout: LIST
   - Order: TIME

2. **news-local:**
   - Description: N/A
   - Layout: LIST
   - Order: TIME

3. **local-forum:**
   - Description: N/A
   - Layout: LIST
   - Order: SCORE

4. **news-cn:**
   - Description: N/A
   - Layout: LIST
   - Order: TIME

5. **cn-podcast:**
   - Description: N/A
   - Layout: LIST
   - Order: TIME

6. **cn-media:**
   - Description: N/A
   - Layout: GRID
   - Order: TIME

7. **news:**
   - Description: News around world
   - Layout: LIST
   - Order: TIME

8. **news-Ukraine:**
   - Description: N/A
   - Search Term: "Ukraine" AND "Russia"
   - Layout: LIST
   - Order: TIME

9. **blogs:**
   - Description: N/A
   - Layout: LIST
   - Order: TIME

10. **tech-blogs:**
    - Description: N/A
    - Layout: LIST
    - Order: TIME

11. **tech-news:**
    - Description: N/A
    - Layout: LIST
    - Order: TIME

... (and more)

For a complete list or details about any specific folder, please let me know!
User input: What happened today based on articles in local-news?    
Calling tool https://grpc-gateway.rssbrain.com//rss.ArticleAPI/GetArticlesByFolderWithUserMarking ...
Assistant: Here are some notable articles from today based on the 'local-news' folder:

1. **A short history of airport gold heists (because Toronto's isn't the first)**
   - **Source:** Canada - CBC News
   - **Description:** Reflection on Toronto's Pearson International Airport's gold heist and similar historical events.
   - [Read more](https://www.cbc.ca/news/world/airport-gold-heists-history-1.6817567)

2. **Family of Nunavut woman says she was used to get Ontario twins Inuit status**
   - **Source:** Canada - CBC News
   - **Description:** A story about a family's reaction after a woman is allegedly used in a fraud to gain Inuit status for twins.
   - [Read more](https://www.cbc.ca/news/canada/north/ontaro-twins-inuit-status-claims-1.6814782)

3. **CBC partners with Markham Library to offer youth Radio Camp**
   - **Source:** Toronto - CBC News
   - **Description:** A creative initiative by CBC for youth in Markham, involving sound exploration and radio production.
   - [Read more](https://www.cbc.ca/news/canada/toronto/community/cbc-partners-with-markham-library-to-offer-youth-radio-camp-1.6806360)

4. **Federal government says it will restore funding to Hockey Canada — with conditions**
   - **Source:** Canada - CBC News
   - **Description:** Funding for Hockey Canada is restored with stipulations after controversy over handling of claims.
   - [Read more](https://www.cbc.ca/news/politics/hockey-canada-federal-government-funding-restored-1.6812492)

5. **Man dies after being hit by tow truck in York Region industrial accident, police say**
   - **Source:** Toronto - CBC News
   - **Description:** Tragic incident of a man being fatally struck by a tow truck, with ongoing investigation.
   - [Read more](https://www.cbc.ca/news/canada/toronto/industrial-accident-york-region-tow-truck-death-1.6780204)

For more details, you can click on the links provided with each summary.
User input: Summarize the first article
Assistant: The first article is titled **"A short history of airport gold heists (because Toronto's isn't the first)"** from "Canada - CBC News". It discusses a recent heist that took place
 at Toronto's Pearson International Airport, where $20 million worth of gold and other valuables were stolen. This event is compared to similar high-profile historical heists at airports, highlighting
 recurring vulnerabilities in airport security regarding valuable shipments. The article provides context and details on how such heists have occurred before in different parts of the world.

```

Since it's pretty expensive when including the OpenAPI doc, I didn't try it much. But there are a few things learnt:

* Good document is really important. LLM doesn't include some parameters if you don't clarify on why it's needed. I think it's actually a good thing, since good document is not only for machines, but also for other people.
* Structure output is not always forced. The json schema also needs to be in the system prompt to make it more reliable.
* APIs need to be designed around the potential use cases. For example, the get articles from a feed API doesn't have a time range since the UI of RSS Brain is infinite scroll. So it's hard for LLM to get the articles in a specific time range, for example, last day.

But overall, I think it's safe to say with the additional authentication flow that can be added by third party, it's easy to integrate any OpenAPI server as a tool to LLM agent without needing any new protocol.

## More About Protocol

I want to add more details about choosing OpenAPI as the protocol. The obvious reason is it has been around for a long time and lots of tools and frameworks support it. But for RSS Brain, I only have a gRPC server. I implemented the gRPC server with my own library [scala2grpc](https://github.com/wb14123/scala2grpc) and really liked it. However, a downside of gRPC is the serialized message is not human readable. So it's hard to define a generic call tool structure. Also it's more difficult to call the API without generated gRPC code. So at the end, I used [grpc-gateway](https://github.com/grpc-ecosystem/grpc-gateway/tree/main) to proxy the gRPC server into an OpenAPI server. The code is released in [Github](https://github.com/wb14123/rss_brain_grpc_gateway).

grpc-gateway has a tool to auto generate OpenAPI structured document, if you have the necessary comments in the gRPC protobuf files. So I added the feature in scala2grpc to add comments based on Scaladoc and it turned out wonderfully.

With all that said, I still think gRPC has its advantage because of the ability of bi-directional streaming. The proto definition is also easier to understand for human, which is a plus for LLMs. Maybe with some more work and leverage things like [gRPC reflection](https://grpc.io/docs/guides/reflection/), we can also define a generic tool calling action with gRPC in the agent.


## Future Work

This is only an experiment, so there are lots of work left to do to make it better.

The first one is to have a better authentication flow, so that the auth URL can give the instructions to LLM agent without user copying it. This is not a hard thing to do but makes it much more secure.

Another thing to try is to not include all the OpenAPI doc in the prompt at first. Instead, only include a summarization or description of it, and only gives more details when LLM agent asks for it. It should be able to save lots of cost.

And there can be more validations for security purpose, for example, when calling a tool or let user open an auth URL in the browser, verify the hostname matches what we have defined in the tools.

It also worth trying other protocols like gRPC since it supports bi directional stream, which may works better in some use cases.
