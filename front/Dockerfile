FROM node:20 AS builder

WORKDIR /app

COPY package*.json ./

RUN npm config set strict-ssl false
RUN npm cache clean --force
RUN npm install

COPY . .

RUN npx ng build

FROM node:20-alpine

WORKDIR /app
RUN npm install -g http-server

COPY --from=builder /app/dist/front/browser ./browser

EXPOSE 4200
CMD ["http-server", "./browser", "-p", "4200", "-a", "0.0.0.0"]