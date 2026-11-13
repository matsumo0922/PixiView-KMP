# Reviews
アプリのレビューに関するログ

## request_review
Google Play のアプリ内レビューで requestReviewFlow の結果が返ったときのログ。成功したときだけ launchReviewFlow に進む

- is_success: !boolean
  - requestReviewFlow が成功し、ReviewInfo を取得できたか

## try_request_review
レビュー依頼の発火条件に到達したときのログ。ダウンロード予約が空から空でない状態に変わったときに 1 回送る

- is_eligible: !boolean
  - 起動回数が 3 回以上で、requestReviewFlow に進んだか。false は起動回数の条件で見送った

## reviewed
launchReviewFlow が完了したときのログ。API はレビューの有無を返さないため、ユーザーがレビューを書いたことは表さない
