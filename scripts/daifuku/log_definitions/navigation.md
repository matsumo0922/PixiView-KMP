# Navigation
アプリ内の画面遷移に関するイベント

## navigate
画面遷移したときのログ

- screen_route: !string 2000
  - 遷移先の画面
- referer: !string 2000
  - 遷移元の画面

## open_url
外部リンクを開いたときのログ

- url: !string 2000
  - 開いたURL
- referer: !string 2000
  - 遷移元の画面
- is_success: !boolean
  - 開けたかどうか

