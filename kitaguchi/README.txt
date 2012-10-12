オープンキャンパスで使ったデモシステムのソースです

・索引はrubiconの /home/kitaguchi/ProRin/data/TwitterIndex を使っています．

・索引を作るにあたって，元々のjsonファイルから，必要なフィールドを抽出してテキストファイルにしました．作成したファイルは /home/kitaguchi/ProRin/data/kita_fields/ 以下にあります．
・各フィールドはタブ区切りで，形式は以下のようになっています．
created_at   screen_name   profile_image_url   in_reply_to_screen_name   text
・tweetに含まれる改行(\nと\r)は半角スペースに置き換えました．
