# Discord Bolt Bot

## Invite URL
(Replace `INSERT_CLIENT_ID_HERE` with your app's client ID)  
https://discord.com/oauth2/authorize?client_id=INSERT_CLIENT_ID_HERE&scope=bot&permissions=277293877248

## Setup

docker-compose.yml
```yml
version: '3.7'

services:
  bolt-bot:
    image: ghcr.io/techtony96/discord-bolt-bot:master
    restart: unless-stopped
    environment:
      - BOT_TOKEN=foo
```
