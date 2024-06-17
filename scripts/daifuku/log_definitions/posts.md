# Posts
投稿に関する捜査のログです。

## download
投稿をダウンロードした際のログ

- type: !string 2000
  - ダウンロードのタイプ (image or file)
- post_id: !string 2000
  - 投稿のID
- item_id: !string 2000
  - アイテムのID
- extension: !string 2000
  - ファイルの拡張子
- is_success: !boolean
  - ダウンロードに成功したかどうか

## like
投稿にいいねをした際のログ

- post_id: !string 2000
  - 投稿のID

## comment
投稿にコメントをした際のログ

- post_id: !string 2000
  - 投稿のID
- parent_comment_id: !string 2000
  - 親コメントのID
- root_comment_id: !string 2000
  - ルートコメントのID
- comment: !string 2000
  - コメントの内容

## like_comment
コメントにいいねをした際のログ

- post_id: !string 2000
  - 投稿のID
- comment_id: !string 2000
  - コメントのID

## delete_comment
コメントを削除した際のログ

- post_id: !string 2000
  - 投稿のID
- comment_id: !string 2000
  - コメントのID