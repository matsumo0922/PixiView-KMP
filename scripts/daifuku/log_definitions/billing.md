# Billing
アプリ内購入に関するログです

## paywall_shown
Plus 課金画面を開き、プランの取得が終わったときのログ。課金画面を 1 回開くごとに 1 回送る

- referrer: !string 32
  - 課金画面を開いた入口。BillingReferrer の値（drawer、onboarding など）
- initial_plan: !string 16
  - 初期選択のプラン（monthly、annual）
- is_plan_loaded: !boolean
  - プランを取得できたか。false のときは購入できないエラー画面を表示している
- days_since_first_launch: !integer
  - 初回起動から課金画面を開くまでの経過日数（切り捨て）。初回起動の日時が未記録なら -1

## paywall_plan_selected
課金画面でプランを選び直したときのログ。選択中と同じプランを選んだときは送らない

- referrer: !string 32
  - 課金画面を開いた入口
- plan: !string 16
  - 選び直した後のプラン（monthly、annual）

## purchase_started
購入ボタンを押し、ストアの購入処理を開始したときのログ。購入処理の完了までは次の購入を開始しない

- referrer: !string 32
  - 課金画面を開いた入口
- plan: !string 16
  - 購入しようとしたプラン（monthly、annual）
- has_trial: !boolean
  - 無料トライアル付きのプランか

## purchase_succeeded
購入が成立し、Plus が有効になったときのログ

- referrer: !string 32
  - 課金画面を開いた入口
- plan: !string 16
  - 購入したプラン（monthly、annual）
- has_trial: !boolean
  - 無料トライアル付きのプランか

## purchase_cancelled
ユーザーがストアの購入画面で購入をキャンセルしたときのログ

- referrer: !string 32
  - 課金画面を開いた入口
- plan: !string 16
  - 購入しようとしたプラン（monthly、annual）

## purchase_failed
キャンセル以外の理由で購入が成立しなかったときのログ

- referrer: !string 32
  - 課金画面を開いた入口
- plan: !string 16
  - 購入しようとしたプラン（monthly、annual）
- error_type: !string 64
  - 失敗の種別。RevenueCat の PurchasesErrorCode の名前（StoreProblemError など）、または product_not_found（プランの商品情報がない）、entitlement_inactive（購入後も Plus が有効にならない）、unknown（それ以外の例外）

## paywall_dismissed
購入が成立しないまま課金画面を閉じたときのログ。購入が成立したときは送らない

- referrer: !string 32
  - 課金画面を開いた入口
- last_purchase_result: !string 16
  - 閉じる前の最後の購入の結果。none（購入を開始していない）、pending（結果が出る前に閉じた）、cancelled、failed、restored（購入の復元で Plus が有効になった）

## retention_prompt_shown
解約予約ユーザー向けリテンション UI を表示したときのログ

- plan_type: !string 32
  - RevenueCat から判定した契約プラン
- unsubscribe_detected_at_millis: bigint
  - RevenueCat が解約予約を検知した日時（ミリ秒）
- is_annual_offer_shown: !boolean
  - 年額プランへの変更導線を表示したかどうか

## retention_prompt_annual_clicked
リテンション UI で年額プランへの変更導線を選択したときのログ

## retention_prompt_manage_clicked
リテンション UI で購読管理導線を選択したときのログ

- platform: !string 32
  - 購読管理画面を開いたプラットフォーム

## retention_prompt_dismissed
リテンション UI を閉じたときのログ

- reason: !string 32
  - UI を閉じた操作
