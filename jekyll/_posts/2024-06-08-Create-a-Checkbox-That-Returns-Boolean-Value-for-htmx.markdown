---
layout: post
title: Create a Checkbox That Returns Boolean Value for htmx
tags: [HTML, htmx, Javascript, frontend, tech]
index: ["/Computer Science/UI/Javascript"]
---

## The Problem of Checkbox

[htmx](https://htmx.org/) is a lightweight Javascript framework. We all know in native HTML, a `form` element can send a HTTP request to a server with the values of `input` elements. In htmx, this feature is made more powerful and flexible: you can include the value of any element, and with the help with htmx extensions like [json-enc](https://htmx.org/extensions/json-enc/), it can also post JSON data.

However, there is one thing that htmx inherited from the native HTML form behaviour: for checkboxes, it only includes its value when the checkbox is checked. And the default value for checkbox is `"on"` instead of `true` (even though you can change it to another value). I understand this decision because it wants to keep the same behaviour so there is no surprise, but it also makes the backend parsing very inconvenient. The checkbox field needs some special treatment at the backend: you need to know there is a checkbox field so that you can set it to false when it's not submitted with the request, and set it to true otherwise.

In this article, we will explore how to define a custom checkbox element so that it has a boolean value and will always be submitted with the HTTP request. We first explore the implementation for htmx and then for native HTML.

## How htmx Submit the Checkbox Value

In order to make it work with htmx, we first need to know how htmx do the HTTP request with parameters. The document doesn't have a lot of details but we can always check the source code. The code that processes input values is in the function [`processInputValue`](https://github.com/bigskysoftware/htmx/blob/d6afc5b8dbd7213037d0bc4213aa0b7b469bcd62/src/htmx.js#L2549):

```javascript
function processInputValue(processed, values, errors, elt, validate) {
    if (elt == null || haveSeenNode(processed, elt)) {
        return;
    } else {
        processed.push(elt);
    }
    if (shouldInclude(elt)) {
        var name = getRawAttribute(elt,"name");
        var value = elt.value;
        if (elt.multiple && elt.tagName === "SELECT") {
            value = toArray(elt.querySelectorAll("option:checked")).map(function (e) { return e.value });
        }
        // include file inputs
        if (elt.files) {
            value = toArray(elt.files);
        }
        addValueToValues(name, value, values);
        if (validate) {
            validateElement(elt, errors);
        }
    }
    if (matches(elt, 'form')) {
        var inputs = elt.elements;
        forEach(inputs, function(input) {
            processInputValue(processed, values, errors, input, validate);
        });
    }
}
```

So it checks whether the element should be included through function `shouldInclude(elt)` and get its value if so (some additional logic for `select` and `file` but it's not a concern here). In [`shouldInclude`](https://github.com/bigskysoftware/htmx/blob/d6afc5b8dbd7213037d0bc4213aa0b7b469bcd62/src/htmx.js#L2512), it will only include a checkbox if it's checked:

```javascript
function shouldInclude(elt) {
    if(elt.name === "" || elt.name == null || elt.disabled || closest(elt, "fieldset[disabled]")) {
        return false;
    }
    // ignore "submitter" types (see jQuery src/serialize.js)
    if (elt.type === "button" || elt.type === "submit" || elt.tagName === "image" || elt.tagName === "reset" || elt.tagName === "file" ) {
        return false;
    }
    if (elt.type === "checkbox" || elt.type === "radio" ) {
        return elt.checked;
    }
    return true;
}
```

## Create a Custom Checkbox Element with Web Component

I tried to find or write an extension for htmx to include checkbox elements with boolean values, but from what I learnt in [the htmx extension doc](https://htmx.org/extensions/), there is no good way to do that. So I decided to create a custom HTML element that extends `input` to return boolean values for htmx to get.

With [web component](https://developer.mozilla.org/en-US/docs/Web/API/Web_components#attributechangedcallback), we can create a HTML tag that can be used just like any other built-in HTML tags. The [MDN guide](https://developer.mozilla.org/en-US/docs/Web/API/Web_components/Using_custom_elements) does a good job to explain how to do it so I will not repeat it here. I'll just put my implementation of the customized checkbox here:

```javascript
class BooleanCheckbox extends HTMLInputElement {
    constructor() {
        super();
    }

    get checked() {
        return true;
    }

    get value() {
        if (super.checked) {
            return true;
        } else {
            return false;
        }
    }
}

customElements.define("boolean-checkbox", BooleanCheckbox, { extends: "input" });
```

You can see it's very simple. It extends the `input` element. It overwrite `checked` to always return `true` so that htmx will always include it in the request. And for `value`, it returns a boolean depends on `super.checked`. At last it register the customized element as a tag namedj`boolean-checkbox`, so that we can just use it like this in HTML:

```html
<input type="checkbox" is="boolean-checkbox" />Boolean checkbox
```

The `is="boolean-checkbox"` part tells the browser that this is a customized input element.

Here is a complete example:

```html
<!DOCTYPE html>
<html>
  <head>
    <title>htmx boolean checkbox example</title>
    <script src="https://unpkg.com/htmx.org@1.9.12"></script>
    <script src="https://unpkg.com/htmx.org@1.9.12/dist/ext/json-enc.js"></script>
    <script>
      class BooleanCheckbox extends HTMLInputElement {
          constructor() {
              super();
          }

          get checked() {
              return true;
          }

          get value() {
              if (super.checked) {
                  return true;
              } else {
                  return false;
              }
          }
      }
      customElements.define("boolean-checkbox", BooleanCheckbox, { extends: "input" });
    </script>
  </head>

  <body>
    <form>
      <div><input type="checkbox" name="default-checkbox" />Default checkbox</div>
      <div><input type="checkbox" is="boolean-checkbox" name="boolean-checkbox" />Boolean checkbox</div>
      <button hx-post="test-post" hx-ext="json-enc">Submit</button>
    </form>
  </body>
</html>
```

It defines two checkboxes: a native one and a customized one. We use the `json-enc` extension so it will post JSON as request body. When click the submit button, if both of them are unchecked, the post body looks like this:

```json
{"boolean-checkbox":false}
```

And if both are selected, here is the post body:

```json
{"default-checkbox":"on","boolean-checkbox":true}
```

## What About the Native HTML Form Action

The custom element `boolean-checkbox` only works with htmx to post boolean values. If you use native form action like this:

```html
<form action="test-call">
  <input is="boolean-checkbox" type="checkbox" name="boolean-checkbox">Boolean Checkbox</input>
  <button>Submit</button>
</form>
```

The behaviour is still like the native checkbox, which only posts value "on" when it's checked.

Even though I don't use the native form action, it still makes me wonder if I can support it. (Disclaimer: all the code below are experiments and I don't recommend anyone uses it on production without careful tests.)

In fact, there is a way to set form value in web component through [`ElementInternals.setFormValue`](https://developer.mozilla.org/en-US/docs/Web/API/ElementInternals/setFormValue):

```javascript
this.internals = this.attachInternals();
this.internals.setFormValue(this.value);
```

However, in HTML standard, `ElementInternals` is not supported if the custom element is extending a built-in input element. Actually there is a [Github issue](https://github.com/whatwg/html/issues/5166) asking for this feature, and the response to not support it doesn't make sense to me:

> Since Apple's WebKit team's position is that customized builtins shouldn't exist in the first place, we don't support this proposal.

Anyway, it is what it is. So I need to workaround it. The solution I came up is to include another checkbox element as a child instead of inherit it. Here is the code:

```javascript
class BooleanCheckbox extends HTMLElement {

    static formAssociated = true;

    constructor() {
        super();
        this.internals = this.attachInternals();
    }

    connectedCallback() {
        this.shadow = this.attachShadow({mode: "open"});
        const internalCheckbox = document.createElement("input");
        internalCheckbox.setAttribute("type", "checkbox");
        this.getAttributeNames().forEach((name) => {
            internalCheckbox.setAttribute(name, this.getAttribute(name));
        });
        this.shadow.appendChild(internalCheckbox);
        this.internals.setFormValue(internalCheckbox.value);
        internalCheckbox.addEventListener('change', () => {
            this.internals.setFormValue(this.value);
        });
    }

    get checkbox() {
        return this.shadow.querySelector("input[type=checkbox]");
    }


    get checked() {
        return true;
    }

    get value() {
        if (this.checkbox.checked) {
            return true;
        } else {
            return false;
        }
    }

}

customElements.define("boolean-checkbox", BooleanCheckbox);
```

It listens on the `checked` attribute on the child checkbox and update the form value based on it. `static formAssociated = true;` is needed so that we can set form values.

Then in HTML, we can use it like this:

```html
<form action="/test-call">
  <div><input type="checkbox" name="default-checkbox" />Default Checkbox</div>
  <div><boolean-checkbox name="boolean-checkbox"></boolean-checkbox>Boolean Checkbox</div>
  <div><button>Submit</button></div>
</form>
```

When click the submit button, it calls `/test-call?boolean-checkbox=false` if both checkboxes are unchecked and `/test-call?default-checkbox=on&boolean-checkbox=true` if both are checked.
