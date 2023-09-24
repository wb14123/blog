module Jekyll
  class RenderAsciicastTag < Liquid::Tag

    def initialize(tag_name, text, tokens)
      super
      @text = text.strip
    end

    def render(context)
      "<div id=\"cast-#{@text}\"></div>" \
      '<script src="/static/js/asciinema-player.min.js"></script>' \
      "<script>AsciinemaPlayer.create('/static/asciicasts/#{@text}.cast', document.getElementById('cast-#{@text}'), {rows: 10, autoPlay: true});</script>"
    end
  end
end

Liquid::Template.register_tag('asciicast', Jekyll::RenderAsciicastTag)

