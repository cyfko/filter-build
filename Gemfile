source "https://rubygems.org"

gem "jekyll", "~> 4.3"
gem "just-the-docs", "~> 0.7.0"

group :jekyll_plugins do
  gem "jekyll-seo-tag"
  gem "jekyll-sitemap"
  gem "jekyll-feed"
  gem "jekyll-relative-links"
  gem "jemoji"
end

# Windows and JRuby does not include zoneinfo files
platforms :mingw, :x64_mingw, :mswin, :jruby do
  gem "tzinfo", ">= 1", "< 3"
  gem "tzinfo-data"
end

# Performance-booster for watching directories
gem "wdm", "~> 0.1", :platforms => [:mingw, :x64_mingw, :mswin]