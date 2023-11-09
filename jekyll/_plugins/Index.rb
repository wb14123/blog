
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

          cur_index['_post_' + post.data['date'].iso8601 + post.data['title']] = post
        end
      end

      index_arr = []
      idx_map_to_array(index, index_arr, -1, nil)

      site.data['index_arr'] = index_arr
    end

    def idx_map_to_array(index_map, entries, level, parent_id)
      index_map.sort.each do |name, value|
        if name.start_with?('_post_')
          entries.append({'type' => "post", 'post' => value, 'level' => level, 'parent_id' => parent_id})
        else
          entries.append({'type' => "index", 'name' => name, 'level' => level, 'parent_id' => parent_id})
          idx_map_to_array(value, entries, level + 1, name)
        end
      end
    end

  end

end
