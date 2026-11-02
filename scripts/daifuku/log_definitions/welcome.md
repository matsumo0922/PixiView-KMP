# Welcome
アプリのウェルカムスクリーンに関するログ

## first_open
アプリを初めて開いたときのログ

## logged_in
ログインしたときのログ

## logged_out
ログアウトしたときのログ

## completed_onboarding
オンボーディングを完了したときのログ

- start_at: !string
  - オンボーディングを開始した時間

- end_at: !string
  - オンボーディングを完了した時間

- needed_time: !bigint
  - オンボーディングにかかった時間 (秒)

## login_checked
ログイン画面で、ログインできたかを確かめたときのログ

- method: !string 32
  - ログインの方法。web は WebView、session_id は FANBOXSESSID の直接入力
- trigger: !string 32
  - 確かめた契機。redirect は戻り先の URL を開いたとき、fanbox_page はそれ以外の FANBOX のページを開いたとき、help はヘルプからログインを押したとき、input は FANBOXSESSID を入力したとき
- result: !string 32
  - 確かめた結果。logged_in / logged_out / session_unreadable / unverified のいずれか
- error_kind: !string 32
  - 確かめられなかった理由（FanboxErrorKind の名前）。確かめられた場合は空文字

## web_login_closed
WebView のログイン画面を閉じたときのログ

- last_host: !string 2000
  - 最後に読み込んだページのホスト。読み込んでいない場合は空文字
- is_logged_in: !boolean
  - ログインできたかどうか
