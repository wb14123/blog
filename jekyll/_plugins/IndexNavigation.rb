module IndexNavigation
  class Generator < Jekyll::Generator
    def generate(site)
      # Create a mapping from index paths to posts
      index_to_posts = {}
      
      site.posts.docs.each do |post|
        next if post['index'] == nil
        
        post['index'].each do |entry|
          # Clean up the entry path
          entry = entry.strip
          entry = entry[1..-2] if entry.start_with?('/') && entry.end_with?('/')
          entry = entry[1..-2] if entry.start_with?("'") && entry.end_with?("'")
          entry = entry[1..-2] if entry.start_with?('"') && entry.end_with?('"')
          
          if index_to_posts[entry] == nil
            index_to_posts[entry] = []
          end
          index_to_posts[entry] << post
        end
      end
      
      # Sort posts in each index by date (newest first)
      index_to_posts.each do |index_path, posts|
        posts.sort! { |a, b| b.date <=> a.date }
      end
      
      # Add prev/next within index to each post
      site.posts.docs.each do |post|
        next if post['index'] == nil
        
        # For each index this post belongs to, find its prev/next in that index
        post.data['index_prev_next'] = {}
        
        post['index'].each do |entry|
          # Clean up the entry path
          entry = entry.strip
          entry = entry[1..-2] if entry.start_with?('/') && entry.end_with?('/')
          entry = entry[1..-2] if entry.start_with?("'") && entry.end_with?("'")
          entry = entry[1..-2] if entry.start_with?('"') && entry.end_with?('"')
          
          posts_in_index = index_to_posts[entry]
          next if posts_in_index == nil || posts_in_index.length <= 1
          
          # Find this post's position in the index
          post_index = posts_in_index.find_index { |p| p == post }
          next if post_index == nil
          
          # Get previous and next posts (remember: posts are sorted newest first)
          prev_post = (post_index + 1 < posts_in_index.length) ? posts_in_index[post_index + 1] : nil
          next_post = (post_index > 0) ? posts_in_index[post_index - 1] : nil
          
          post.data['index_prev_next'][entry] = {
            'prev' => prev_post,
            'next' => next_post
          }
        end
      end
    end
  end
end