#!/usr/bin/env amm

import $ivy.`com.lihaoyi::requests:0.9.0`
import $ivy.`org.jsoup:jsoup:1.21.1`
import org.jsoup.Jsoup

import java.nio.file.Path
import java.io.PrintWriter
import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters._
import java.time.{Instant, LocalDate, ZoneId}
import scala.util.Try

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
    val filename = title
      .replaceAll("['\"?.!《》/\\\\]", "") // remove all special chars
      .replaceAll("[ .,。，·:：]", "-") // replace seperator chars to `-`
      .replaceAll("-+", "-") // consolidate `-` chars
      .toLowerCase
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
  def download(url: String): Seq[Book]
}

object BookProvider {
  val GOODREADS = "goodreads"
  val DOUBAN = "douban"
  val ALL = Seq(GOODREADS, DOUBAN)
}


class GoodReadsDownloader extends BookDownloader {

  def matches(url: String): Boolean = {
    url.startsWith("https://www.goodreads.com/book/show/")
  }

  def download(url: String): Seq[Book] = {
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
    val book = Book(
      title = title,
      authors = authors,
      isbn = isbn,
      coverUrl = coverUrl,
      publishYear = publishYear,
      externalLinks = Map(BookProvider.GOODREADS -> url),
    )
    Seq(book)
  }
}

class GoodReadListDownloader(bookDownloader: BookDownloader) extends BookDownloader {
  def matches(url: String): Boolean = {
    url.startsWith("https://www.goodreads.com/review/list/")
  }

  def download(url: String): Seq[Book] = {
    val baseUrl = url.split('?').head
    LazyList.from(1).map(page => getBookUrlsAndDateRead(baseUrl, page, 100))
      .takeWhile(_.nonEmpty)
      .flatten
      .map { case (url, dateRead) =>
        Thread.sleep(200) // sleep 100ms for rate limit
        println(s"Downloading book from $url ...")
        bookDownloader.download(url).head.copy(dateRead = dateRead)
      }
  }

  private def parseTimeToInstant(timeString: String): Instant = {
    val fullDatePattern = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val monthYearPattern = DateTimeFormatter.ofPattern("MMM yyyy")

    Try {
      // Try to parse as "Dec 07, 2014"
      LocalDate.parse(timeString, fullDatePattern).atStartOfDay(ZoneId.systemDefault()).toInstant
    }.recover {
      case _ =>
        // Try to parse as "Jan 2015" (first day of month)
        val temporalAccessor = monthYearPattern.parse(timeString)
        val month = temporalAccessor.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
        val year = temporalAccessor.get(java.time.temporal.ChronoField.YEAR)
        LocalDate.of(year, month, 1).atStartOfDay(ZoneId.systemDefault()).toInstant
    }.getOrElse(throw new IllegalArgumentException(s"Cannot parse time string: $timeString"))
  }

  private def getBookUrlsAndDateRead(listBaseUrl: String, page: Int, perPage: Int): Seq[(String, Instant)] = {
    val url = s"$listBaseUrl?&per_page=$perPage&page=$page"
    val res = requests.get(url).text()
    val html = Jsoup.parse(res)
    val body = html.body()
    body.select("#booksBody .bookalike").listIterator().asScala.toList.map { elem =>
      val urlPath = elem.select(".title a").getFirst.attr("href")
      val url = s"https://www.goodreads.com$urlPath"
      val dateReadStr = Option(elem.select(".date_read_value")).filter(_.size() > 0).map(_.getFirst.text())
      val dateRead = dateReadStr.map(parseTimeToInstant).getOrElse(Instant.now())
      (url, dateRead)
    }
  }

}

@main
def main(url: String, path: String): Unit = {
  val goodReadsDownloader = new GoodReadsDownloader()
  val goodReadListDownloader = new GoodReadListDownloader(goodReadsDownloader)
  val downloaders = Seq(goodReadsDownloader, goodReadListDownloader)
  downloaders.find(_.matches(url)) match {
    case None => println(s"No downloader found for link $url")
    case Some(downloader) =>
      println(s"Trying to download from $url ...")
      downloader.download(url).foreach { book =>
        val outputFilePath = book.createJekllyFile(Path.of(path))
        println(s"Book written to $outputFilePath")
      }
  }
}