# Billing
アプリ内購入に関するログです

## purchase
購入リクエスト

- referrer: !string 2000
  - 有効にしようとした設定の値、もしくは遷移元の画面
- is_success: !boolean
  - 購入成功したかどうか

## consume
消費リクエスト

- is_success: !boolean
  - 消費成功したかどうか

## verify
検証リクエスト

- is_success: !boolean
  - 検証成功したかどうか

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
