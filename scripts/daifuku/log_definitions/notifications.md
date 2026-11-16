# Notifications
新着投稿の通知と、通知の権限の案内に関するログ

## new_post_notification_shown
新着投稿の定期確認で、新着投稿の通知を表示したときのログ。通知は投稿ごとに表示し、1 回の確認につき 1 回送る。通知の権限がなく 1 件も表示しなかったときは送らない

- post_count: !integer
  - 1 回の確認で表示した、投稿ごとの通知の件数

## new_post_notification_tapped
新着投稿の通知をタップしてアプリを開いたときのログ

- post_count: !integer
  - タップした通知の新着投稿の件数。投稿ごとの通知（投稿詳細を開く）は 1、グループの親の通知（アプリを開く）は表示中の投稿ごとの通知の件数

## permission_prompt_shown
通知の権限の案内を表示したときのログ。ダイアログは表示するたびに、通知タブの案内は通知タブを開くたびに 1 回送る。通知タブの案内は、画面の回転などで Activity が作り直されたときにも送り直す

- referrer: !string 16
  - 案内の種類（dialog、notify_tab）

## permission_result
通知の権限の案内から、許可を求めた結果が分かったときのログ。Android 13 以上は権限のダイアログの結果、端末の設定画面を開いたときは画面に戻った時点の状態を送る

- referrer: !string 16
  - 案内の種類（dialog、notify_tab）
- is_success: !boolean
  - 通知が許可されたか
