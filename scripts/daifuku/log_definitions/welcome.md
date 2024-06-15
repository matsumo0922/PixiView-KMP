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