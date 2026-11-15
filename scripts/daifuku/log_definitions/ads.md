# Ads
広告に関するログ

## interstitial_show_attempted
投稿詳細を閉じたときに、インタースティシャル広告の表示条件を満たし、表示を試みたときのログ。試みるたびに 1 回送る。表示できたときは、表示が始まった時点で送る

- result: !string 32
  - 表示を試みた結果。shown（表示できた）、loading（広告をロード中）、load_failed（ロードに失敗し、再試行を待っている）、expired（ロードから 50 分を過ぎた広告を破棄した）、show_failed（表示に失敗した）、disabled（広告のロードが無効。表示対象のユーザーでは、Mobile Ads SDK の初期化が終わる前に限られる）
- is_retry: !boolean
  - 前回の試行で表示できなかったため、クローズ回数によらず再試行したか
