import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler(args[0]));
        System.out.println("Starting server on port: " + port);
        server.start();
    }

    static class RootHandler implements HttpHandler {
        String filePath;
        String rootUrl;
        String rootPath;

        public RootHandler(String path) {
            this.filePath = path;
            this.rootUrl = "http://localhost:8000" + path;
            this.rootPath = path;
        }

        public void handle(HttpExchange exchange) throws IOException {
//            System.out.println(filePath);
//            System.out.println(rootPath);
//            System.out.println(rootUrl);

            URI uri = exchange.getRequestURI();
//            System.out.println("URI: " + uri);

            File fileFromUri = new File(rootPath + uri.toString());
            if (fileFromUri.getCanonicalPath().startsWith(rootPath)) {
                filePath = rootPath + uri.getPath();
            }

//            System.out.println("po przypisaniu: " + filePath);

//            String[] filePathArray = filePath.split("/");
//            String[] uriPathArray = uri.getPath().split("/");
//            System.out.println(Arrays.toString(filePathArray));
//            System.out.println(Arrays.toString(uriPathArray));

//          --------------------------------------------------------
//          Czytanie zawartości folderu
            String[] directories = fileFromUri.list(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).exists();
                }
            });

//          --------------------------------------------------------
//          Sprawdzanie czy ścieżka to plik lub folder
                if(!fileFromUri.getCanonicalPath().startsWith(rootPath)) {
                    System.out.println("Brak dostepu!!!");
                    byte[] response = new byte[0];
                    exchange.sendResponseHeaders(403, -1);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response);
                    os.close();
                }
                byte[] response;
//                System.out.println("File from url: " + fileFromUri);
                    if (fileFromUri.isFile()) {
                        response = Files.readAllBytes(Paths.get(filePath));
                        exchange.sendResponseHeaders(200, response.length);
                        System.out.println("PLik zostal otwarty");
                    } else if (fileFromUri.isDirectory()) {
//                        System.out.println(Arrays.toString(directories));
                        String[] linkedDirs =  directories.clone();
                        for (int i = 0; i < directories.length; i++) {
                            if (uri.toString().equals("/")) {
//                                System.out.println("<a href=\"http://localhost:8080" + uri.toString() + directories[i] +"\">"+ directories[i] +"</a>");
                                directories[i] = "<a href=\"http://localhost:8080" + uri.toString() + directories[i] +"\">" + directories[i] + "</a><br />";
                            } else {
//                                System.out.println("<a href=\"http://localhost:8080" + uri.toString() + "/" + directories[i] +"\">"+ directories[i] +"</a>");
                                directories[i] = "<a href=\"http://localhost:8080" + uri.toString() + "/" + directories[i] +"\">" + directories[i] + "</a><br />";
                            }
                        }
                        System.out.println("Folder zostal otworzony");
                        response = Arrays.toString(directories).getBytes();
                        exchange.getResponseHeaders().set("Content-Type", "text/html");
                        exchange.sendResponseHeaders(200, response.length);

                    } else {
                        System.out.println("PLik nie istanieje!!!");
                        response = new byte[0];
                        exchange.sendResponseHeaders(404, -1);
                    }

                    OutputStream os = exchange.getResponseBody();
                    os.write(response);
                    os.close();
        }
    }
}