
module Index
  class Generator < Jekyll::Generator
    def generate(site)
      index = {}
      site.posts.docs.each do |post|
        next if post['index'] == nil
        post['index'].each do |entry|
          cur_index = index
          entry.split('/').each do |level|
            unless level.to_s.strip.empty?
              if cur_index[level] == nil
                cur_index[level] = {}
              end
              cur_index = cur_index[level]
            end
          end

          cur_index['_post_' + post.data['title']] = post
        end
      end

      index_arr = idx_map_to_array(index)

      index_page = site.pages.find { |page| page.name == 'index_page.html'}
      index_page.data['type'] = "root"
      index_page.data['index'] = index_arr
    end

    def idx_map_to_array(index_map)
      root = []
      index_map.each do |name, value|
        if name.start_with?('_post_')
          root.append({'type' => "post", 'post' => value})
        else
          root.append({'name' => name, 'type' => "index", 'value' => idx_map_to_array(value)})
        end
      end
      return root.sort{ |a,b| compare_index(a, b)}
    end

    def compare_index(a, b)
      if a['type'] != b['type']
        return a['type'] <=> b['type']
      elsif a['type'] == 'index'
        return a['name'] <=> b['name']
      else
        return a['post'].data['date'] <=> b['post'].data['date']
      end
    end

  end

end
