#!/usr/bin/env amm

import $ivy.`com.lihaoyi::requests:0.9.0`
import $ivy.`org.jsoup:jsoup:1.21.1`
import org.jsoup.Jsoup

import java.nio.file.Path
import java.io.PrintWriter
import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters._

case class Book(
    title: String,
    authors: Seq[String],
    isbn: Option[String],
    coverUrl: String,
    publishYear: Option[Int],
    externalLinks: Map[String, String],
    dateRead: Instant = Instant.now(),
) {
  private def removeEmptyLines(str: String): String = str.replaceAll("\n\n+", "\n").replaceAll("\n\n+$", "")

  def toLiquidHeader: String = {
    val header = s"""---
       |layout: book
       |title: "$title"
       |authors: [${authors.map(a => s"\"$a\"").mkString(", ")}]
       |${isbn.map(isbn => s"isbn: \"$isbn\"").getOrElse("")}
       |cover: "$coverUrl"
       |${publishYear.map(year => s"year: $year").getOrElse("")}
       |external_links:{${externalLinks.map { case (provider, url) => s"\"$provider\": \"$url\"" }.mkString(",")}}
       |date_read: "${dateRead.atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
       |---
       |""".stripMargin
    removeEmptyLines(header)
  }

  def createJekllyFile(path: Path): String = {
    val filename = title.split(":").head.replaceAll(" +", "-").toLowerCase
    val file = path.resolve(s"$filename.md").toFile
    println(s"Creating file ${file.getAbsolutePath}")
    val writer = new PrintWriter(file)
    writer.write(toLiquidHeader)
    writer.close()
    file.getAbsolutePath
  }
}

trait BookDownloader {
  def matches(url: String): Boolean
  def download(url: String): Book
}

object BookProvider {
  val GOODREADS = "goodreads"
  val DOUBAN = "douban"
  val ALL = Seq(GOODREADS, DOUBAN)
}


class GoodReadsDownloader extends BookDownloader {

  def matches(url: String): Boolean = {
    return url.startsWith("https://www.goodreads.com/")
  }

  def download(url: String): Book = {
    val res = requests.get(url).text()
    val html = Jsoup.parse(res)
    val body = html.body()
    val title = body.select("[data-testid=bookTitle]").getFirst.text()
    val authors = body.select(".ContributorLink__name[data-testid=name]").listIterator()
      .asScala.map(_.text()).toList.distinct
    val coverUrl = body.select(".BookCover__image img").getFirst.attr("src")
    val jsonScript = html.select("script[type=application/ld+json]").first().data()
    val isbnPattern = """"isbn":"([^"]+)"""".r
    val isbn = isbnPattern.findFirstMatchIn(jsonScript).map(_.group(1))
    val yearPattern = """.*?(\d{4}).*""".r
    val publishYear = Option(body.select("p[data-testid=publicationInfo]").first())
      .flatMap { elem =>
        yearPattern.findFirstMatchIn(elem.text()).map(_.group(1).toInt)
      }
    Book(
      title = title,
      authors = authors,
      isbn = isbn,
      coverUrl = coverUrl,
      publishYear = publishYear,
      externalLinks = Map(BookProvider.GOODREADS -> url),
    )
  }
}

@main
def main(url: String, path: String): Unit = {
  val downloaders = Seq(new GoodReadsDownloader())
  downloaders.find(_.matches(url)) match {
    case None => println(s"No downloader found for link $url")
    case Some(downloader) =>
      println(s"Downloading book from $url ...")
      val outputFilePath = downloader.download(url).createJekllyFile(Path.of(path))
      println(s"Book written to $outputFilePath")
  }
}