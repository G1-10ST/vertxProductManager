package com.miko.ProductManager;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class MainVerticle extends AbstractVerticle {

  private MySQLPool client;

  @Override
  public void start(Promise<Void> startPromise) {

    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setHost("localhost")
      .setPort(3306)
      .setDatabase("order_manager")
      .setUser("appuser")
      .setPassword("Password123!");

    PoolOptions pool = new PoolOptions().setMaxSize(5);

    client = MySQLPool.pool(vertx,connectOptions,pool);

    HttpServer server = vertx.createHttpServer();


    Router router = Router.router(vertx);

    router.get("/getAllProducts").handler(context ->
      client.query("SELECT * FROM Product").execute(result -> {
      if(result.succeeded()) {
        RowSet<Row> dbResult = result.result();
        JsonArray response = new JsonArray();
        for(Row row : dbResult) {
          response.add(new JsonObject()
            .put("id",row.getInteger("id"))
            .put("description",row.getString("description"))
            .put("name",row.getString("name"))
            .put("price",row.getInteger("id"))
            .put("stock",row.getInteger("available_quantity"))
          );
        }
        context.response().end(response.encodePrettily());
      } else {
        JsonObject response = new JsonObject();
        response.put("errorCode" , "DB-500");
        response.put("errorMessage" , "Could not recieve data");
        context.response().setStatusCode(500).end(response.encodePrettily());
      }
    }));

    server.requestHandler(router).listen(8080);
    System.out.println("Server started on port : "+server.actualPort());
  }

}
