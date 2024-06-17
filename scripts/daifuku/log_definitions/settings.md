# Settings
アプリの設定に関するログ

## init
アプリの設定が初期化された際に出力されるログ

- settings: !string 2000
  - 初期化された設定の内容

## update
アプリの設定が更新された際に出力されるログ

- property_name: !string 2000
  - 更新された設定の名前
- old_value: !string 2000
  - 更新前の値
- new_value: !string 2000
  - 更新後の値