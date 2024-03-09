## FANBOXSESSID 取得方法
FANBOXSESSIDとは、いわゆるpixivFANBOX内で使用されているセッションIDです。セッションIDはユーザーの認証状態を保持するために用いられるIDで、これが漏洩すると言うことはあなたのアカウントに誰かが無断でアクセスできてしまうと言うことを指します。

そのため、セッションIDを手動で取得することはセキュリティリスクが高く、本来推奨されない方法です。可能であれば、アプリ内からのログインをお試しください。

## 1. 準備
- PC
- Chrome（他のブラウザでも可能ですがここでは説明しません）

## 2. FANBOXSESSIDを生成する
先に説明したとおり、FANBOXSESSIDとはユーザーの認証状態を保持するためのIDです。そのため、まずはじめにpixivFANBOXへのログインを行い、FANBOXSESSIDを生成する必要があります。以下のログインでは、アプリではログインできなかったアカウント（アプリで使用したいアカウント）で行ってください。Google,Apple,X もしくは E-mail, Pixiv ID どの認証手段を用いても構いません。

1. 用意したPCのChromeで [pixivFANBOX](https://www.fanbox.cc/)にアクセスする
2. 右上に「ログイン」と言うボタンが表示されていたらクリックし、ログインを行う（ログイン済みであれば3へ）
3. 支援しているクリエイターやフォローしているクリエイターの投稿を何件か見る

## 3. FANBOXSESSIDを取得する
生成されたFANBOXSESSIDはCookieとしてPCに保存されています。これを取得する必要があります。以後の操作は先に説明したセキュリティリスクがあるため、細心の注意を払ってください。間違っても、FANBOXSESSIDが漏洩するような行為（ex: SNSに上げる, Yahoo知恵袋で質問する）は行わないでください。

1. 開発者ツールを起動する（何もないところを右クリック→「検証」をクリック, もしくはF12キーを押す）
2. 出てきたウィンドウの上部に並んでいるタブの中から「アプリケーション」もしくは「Application」を探してクリックする
3. 左ペインの中から「Cookie」→「 https://www.fanbox.cc 」を探してクリックする
4. Cookieが何個か表示されるので、名前の列から「FANBOXSESSID」を探し、「値」にある文字列をコピーする

|手順2|手順3|手順4|
|-|-|-|
|![スクリーンショット 2024-03-10 015904](https://github.com/matsumo0922/PixiView-KMP/assets/56629437/6c775d2a-bb96-426a-9034-e3baa8841262)|![スクリーンショット 2024-03-10 015944](https://github.com/matsumo0922/PixiView-KMP/assets/56629437/0a9fec6a-2186-4fbc-8176-9bd3d9568c0b)|![スクリーンショット 2024-03-10 020035](https://github.com/matsumo0922/PixiView-KMP/assets/56629437/9567c16b-0fc1-4599-98e9-63fadc2272c6)|

## 4. アプリでログインを行う
コピーした値をメモするなりして記録した後、その文字列をそのままアプリに打ち込みます。このとき、余計な文字列（空白なども含む）は足さないでください。PCに表示されている値とアプリに記入した値が一致していることを確認したら、ログインボタンを押してください。FANBOXSESSIDの認証情報を用いてアプリが自動的にログインを行うはずです。「ログイン済みです」と表示されたら、ログインは正常に完了しています。そのままアプリの操作を続行してください。

もしこの方法を用いてもログインが成功しない場合は、アプリの不具合もしくはpixivFANBOXのサーバー側の不具合の可能性があります。[issue](https://github.com/matsumo0922/PixiView-KMP/issues/new)より、デバイス名やOSバージョンを明記してお気軽にお尋ねください。FANBOXSESSIDは絶対に書かないでください。