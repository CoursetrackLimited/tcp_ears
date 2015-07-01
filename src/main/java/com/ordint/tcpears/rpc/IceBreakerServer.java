package com.ordint.tcpears.rpc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



public class IceBreakerServer
 {
/*  84 */   private ServerSocket providerSocket = null;
/*  85 */   private Socket connection = null;
   private PrintWriter pw;
   private String ContentType;
   private String Status;
/*  89 */   private StringBuilder resp = new StringBuilder(1024);
/*  90 */   private Boolean doFlush = Boolean.valueOf(false);
   
   private int Port;
   
   private int Queue;
   
   private InputStream in;
   
   public String request;
   
   public String payload;
   
   public String method;
   
   public String queryStr;
   
   public String resource;
   public String httpVer;
/* 108 */   public Boolean debug = Boolean.valueOf(false);
   
 
/* 111 */   public Map<String, String> header = new HashMap<>();
   
/* 113 */   public Map<String, String> parms = new HashMap<>();
   
   private void loadProps() {
/* 116 */     Properties prop = new Properties();
     
     try
     {
/* 120 */       prop.load(new FileInputStream("config.properties"));
/* 121 */       this.Port = Integer.parseInt(prop.getProperty("restserver.port", "65000"));
/* 122 */       this.Queue = Integer.parseInt(prop.getProperty("restserver.queuesize", "10"));
     }
     catch (IOException ex) {}
   }
   
 
 
   public IceBreakerServer()
   {
/* 131 */     loadProps();
   }
   
 
 
 
 
   public void setContentType(String s)
   {
/* 140 */     this.ContentType = s;
   }
   
 
 
 
   public void setStatus(String s)
   {
/* 148 */     this.Status = s;
   }
   
 
 
 
 
 
   public void setPort(int port)
   {
/* 158 */     this.Port = port;
   }
   
 
 
 
 
 
   public void setQueue(int queue)
   {
/* 168 */     this.Queue = queue;
   }
   
 
 
 
 
 
 
 
   public String getQuery(String Key, String Default)
   {
/* 180 */     String temp = (String)this.parms.get(Key);
/* 181 */     if (temp == null) return Default;
/* 182 */     return temp;
   }
   
 
 
 
 
 
 
   public String getQuery(String Key)
   {
/* 193 */     return (String)this.parms.get(Key);
   }
   
 
 
 
 
 
   public String now()
   {
/* 203 */     Date date = new Date();
/* 204 */     Format formatter = new SimpleDateFormat("hh:mm:ss");
/* 205 */     String s = formatter.format(date);
/* 206 */     return s;
   }
   
   private static Map<String, String> getQueryMap(String query) {
/* 210 */     String[] params = query.split("&");
/* 211 */     Map<String, String> map = new HashMap<>();
/* 212 */     for (String param : params) {
/* 213 */       int p = param.indexOf('=');
/* 214 */       if (p >= 0) {
/* 215 */         String name = param.substring(0, p);
/* 216 */         String value = param.substring(p + 1);
/* 217 */         String s = URLDecoder.decode(value);
/* 218 */         map.put(name, s);
       }
     }
/* 221 */     return map;
   }
   
 
   private int isEol(byte[] buf, int i)
   {
/* 227 */     if ((buf[i] == 13) && (buf[(i + 1)] == 10)) {
/* 228 */       if ((buf[(i + 2)] == 13) && (buf[(i + 3)] == 10)) {
/* 229 */         return -4;
       }
/* 231 */       return 2;
     }
/* 233 */     if (buf[i] == 13) {
/* 234 */       if (buf[(i + 1)] == 13) {
/* 235 */         return -2;
       }
/* 237 */       return 1;
     }
/* 239 */     if (buf[i] == 10) {
/* 240 */       if (buf[(i + 1)] == 10) {
/* 241 */         return -2;
       }
/* 243 */       return 1;
     }
/* 245 */     return 0;
   }
   
   private void unpackRequest()
     throws IOException
   {
/* 251 */     byte[] buf = new byte[32768];
/* 252 */     this.in = this.connection.getInputStream();
/* 253 */     int read = this.in.read(buf);
/* 254 */     int len = 0;int pos = 0;int eol = 0;
/* 255 */     this.header.clear();
/* 256 */     this.parms.clear();
/* 257 */     this.request = (this.payload = this.method = this.queryStr = this.httpVer = this.resource = null);
/* 258 */     for (int i = 0; (i < read) && (eol >= 0); i++) {
/* 259 */       eol = isEol(buf, i);
/* 260 */       if (eol > 0)
       {
/* 262 */         if (this.request == null) {
/* 263 */           this.request = new String(buf, pos, len);
/* 264 */           String[] temp = this.request.split(" ");
/* 265 */           this.method = temp[0];
/* 266 */           this.queryStr = temp[1];
/* 267 */           this.httpVer = temp[2];
/* 268 */           int p = this.queryStr.indexOf('?');
/* 269 */           if (p >= 0) {
/* 270 */             this.resource = this.queryStr.substring(0, p);
/* 271 */             this.parms = getQueryMap(this.queryStr.substring(p + 1));
           } else {
/* 273 */             this.resource = this.queryStr;
           }
         }
         else {
/* 277 */           String param = new String(buf, pos, len);
/* 278 */           int p = param.indexOf(':');
/* 279 */           String name = param.substring(0, p);
/* 280 */           String value = param.substring(p + 1);
/* 281 */           this.header.put(name, value.trim());
         }
/* 283 */         len = 0;
/* 284 */         pos = i + eol;
/* 285 */         i += eol - 1;
/* 286 */       } else if (eol < 0) {
/* 287 */         pos = i + -eol;
/* 288 */         this.payload = new String(buf, pos, read - pos);
       } else {
/* 290 */         len++;
       }
     }
     
 
/* 295 */     if (this.debug.booleanValue()) {
/* 296 */       System.out.println("resource: " + this.request);
/* 297 */       System.out.println("method: " + this.method);
/* 298 */       System.out.println("resource: " + this.resource);
/* 299 */       System.out.println("queryStr: " + this.queryStr);
/* 300 */       System.out.println("httpVer: " + this.httpVer);
/* 301 */       System.out.println("header  : " + this.header);
/* 302 */       System.out.println("parms : " + this.parms);
     }
   }
   
 
   private void sendResponse()
   {
/* 309 */     this.pw.print("HTTP/1.1 " + this.Status + "\r\n" + "Connection: Keep-Alive\r\n" + "Accept: multipart/form-data\r\n" + "Accept-Encoding: multipart/form-data\r\n" + "Server: IceBreak Java Services\r\n" + "cache-control: no-store\r\n" + "Content-Length: " + Integer.toString(this.resp.length()) + "\r\n" + "Content-Type: " + this.ContentType + "\r\n" + "\r\n" + this.resp.toString());
     
 
 
 
 
 
 
 
/* 318 */     this.pw.flush();
   }
   
 
 
   public void getHttpRequest()
     throws IOException
   {
/* 326 */     if (this.providerSocket == null) {
/* 327 */       this.providerSocket = new ServerSocket(this.Port, this.Queue);
     }
/* 329 */     if (this.doFlush.booleanValue()) { flush();
     }
/* 331 */     this.connection = this.providerSocket.accept();
/* 332 */     this.pw = new PrintWriter(this.connection.getOutputStream());
/* 333 */     this.resp.setLength(0);
/* 334 */     unpackRequest();
/* 335 */     this.ContentType = "text/plain; charset=utf-8";
/* 336 */     this.Status = "200 OK";
/* 337 */     this.doFlush = Boolean.valueOf(true);
   }
   
 
 
 
 
   public void write(String s)
   {
/* 346 */     this.resp.append(s);
   }
   
 
   public void flush()
     throws IOException
   {
/* 353 */     sendResponse();
/* 354 */     this.connection.close();
/* 355 */     this.doFlush = Boolean.valueOf(false);
   }
 }
 