# Billing
アプリ内購入に関するログです。課金画面のイベント（paywall_shown など）に置き換えたため、次のイベントは送っていない

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
