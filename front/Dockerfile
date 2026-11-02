FROM node:20 AS builder

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npx ng build

FROM node:20-alpine

WORKDIR /app
RUN npm install -g serve@14.2.6

COPY --from=builder /app/dist/front/browser ./browser

EXPOSE 4200
# -s is the single-page-app mode: unknown paths fall back to index.html, so
# refreshing or deep-linking an Angular route works in the Docker demo.
CMD ["serve", "-s", "./browser", "-l", "tcp://0.0.0.0:4200"]