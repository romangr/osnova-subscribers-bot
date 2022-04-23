# subscribers-bot

A telegram bot to notify TJ, VC and DTF users about their subscribers (currently - about users that have unsubscribed from their blog)

## Usage

### Environment varibles

```
TELEGRAM_TOKEN=<telegram bot token>
TJ_TOKEN=<API token>
VC_TOKEN=<API token>
DTF_TOKEN=<API token>
DB_FILE_PATH=local.db
```

### Start docker container

```
docker run -d \
  -m 256M \
  --name osnova_subscribers_bot \
  -v `pwd`/osnova_db/local.db:/bot/local.db \
  --log-driver json-file \
  --log-opt max-size=1m \
  --log-opt max-file=3 \
  --env-file osnova.env \
  ghcr.io/romangr/osnova-subscribers-bot:latest
```

## License

Copyright © 2021 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
