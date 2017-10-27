## Overview

Quickstart/template for a Clojure/Ring webserver with Datomic.

Datomic is a novel database with unique advantages. Read about it on
http://www.datomic.com.

## Usage

  * clone and `cd` to the repo
  * [setup Datomic](#datomic-setup)
  * setup [env secrets](#env-secrets)
  * [run](#run)


### Datomic Setup

1. Get Datomic license

Register on http://www.datomic.com.

We're using Datomic Pro Starter. The website variously shortens it to "Datomic
Starter", and it also has a "Datomic Free". Navigate this maze with confidence:
Datomic Pro Starter _is_ free, and is exactly what we want.

2. Download

Go to https://my.datomic.com/downloads/pro and download the _exact_ same version
as our Datomic dependency in `project.clj`:
https://my.datomic.com/downloads/pro/0.9.5561. Unzip it somewhere. The download
includes:

  * transactor server
  * peer library for use in our app process

Unzip and navigate to the resulting folder:

```sh
cd <unzipped folder>
```

3. Make peer library available

The peer library is the dependency `com.datomic/datomic-pro` in `project.clj`.
It's what our app uses to connect to Datomic and run queries. It's not in a
public repository, and comes with the Datomic download. Assuming you have Maven
installed, run this command:

```sh
bin/maven-install
```

This makes it locally available to Leiningen.

Note: you can connect to Datomic as a _client_ or a _peer_. A client runs every
request over the network, while a peer caches data locally. Being a peer costs
memory, but gives you better read performance and negates network latency
between app and database. Peer are also fantastic for horizontal scaling.
Unfortunately the official Datomic tutorial focuses on a client, while being a
peer is what you really want, and that's what we're doing.

4. Setup config

We'll be using the self-sufficient "dev" configuration. Copy the template:

```sh
cp config/samples/dev-transactor-template.properties config/transactor.properties
```

5. Add license keys

You need to add the Datomic license key to two property files:

  * `config/transactor.properties` in the unzipped Datomic folder
  * `config/transactor.properties` in the app repository

Open the files and fill the `license-key=` field. To get the key, go to
https://my.datomic.com/account and click "Send License Key" to receive it in an
email. This is _not_ the short download key immediately visible on the page.

6. Run transactor

```sh
bin/transactor config/transactor.properties
```

This is the core database process. Keep it running forever.

In the "dev" configuration, this process is self-sufficient and manages storage
by itself, using local files. In other configurations, you also need to manage
the storage database the transactor connects to.

7. Create database

The app will do this automatically. See `src/app/dat.clj` → `Dat` → `start`.

8. Transact schema

The app will do this automatically. See `src/app/dat.clj` → `Dat` → `start`.


### Env Secrets

Copy or rename `.env.properties.example` → `.env.properties`:

```sh
cp .env.properties.example .env.properties
```

Unless you did something weird in your Datomic setup, you can leave the
values as-is.


### Run

Now you can run the app:

```sh
lein repl
```

Or:

```sh
lein repl :headless
# another tab
lein repl :connect
```

If you have completed all previous steps, this should launch the app and report
a localhost URL to open. It should display a webpage with the database status, a
comment form, and a comment list.

## Misc

If you have question or suggestions, open an issue, reach me on
skype:mitranim.web, or email to me@mitranim.com.
