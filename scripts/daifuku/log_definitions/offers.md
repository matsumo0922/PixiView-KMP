# Offers
ホームの投稿一覧の最上部の、オファー（無料トライアル、初月割引）を案内するバナーに関するログです

## banner_shown
オファーのバナーを表示したときのログ。ホーム画面の表示中は、オファーの種類と周期の組ごとに 1 回だけ送る

- offer_type: !string 16
  - バナーで案内したオファーの種類。free_trial（無料トライアル）、intro_offer（初月割引などの導入価格）
- offer_cycle: !integer
  - 初回起動から何回目の提示の周期か（10 日ごとの区切り、最初の周期を 1 とする）

## banner_tapped
オファーのバナーを押し、課金画面を開いたときのログ。課金画面のログの referrer は offer_banner になる

- offer_type: !string 16
  - バナーで案内したオファーの種類
- offer_cycle: !integer
  - 初回起動から何回目の提示の周期か

## banner_dismissed
オファーのバナーを閉じたときのログ。閉じた周期の間は、バナーを表示しない

- offer_type: !string 16
  - バナーで案内したオファーの種類
- offer_cycle: !integer
  - 初回起動から何回目の提示の周期か
