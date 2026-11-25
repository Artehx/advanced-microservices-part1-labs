package com.advancedorders.inventory.state;

import java.sql.*;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/inventory/training/state")
@ConditionalOnProperty(name="training.state.enabled", havingValue="true")
public class InventoryStateController {
    private final String db,user,password;
    public InventoryStateController(@Value("${training.state.db}") String db,@Value("${training.state.user}") String user,@Value("${training.state.password}") String password){
        this.db=db;this.user=user;this.password=password;
    }
    private Connection connect() throws SQLException{return DriverManager.getConnection(db,user,password);}
    public record Request(String orderId,String sku,int quantity){}
    private void validate(Request r){if(r.orderId()==null||r.orderId().isBlank()||r.sku()==null||r.sku().isBlank()||r.quantity()<1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid reservation");}
    // Serialized per process in this lab; stock row is also locked in the DB transaction.
    @PostMapping("/reserve")
    public synchronized Map<String,String> reserve(@RequestBody Request r)throws SQLException{
        validate(r);
        try(var c=connect()){
            c.setAutoCommit(false);
            try{
                String status=existing(c,r);
                if(status!=null){c.commit();return Map.of("status",status);}
                int available=-1;
                try(var p=c.prepareStatement("SELECT available FROM inventory.stock WHERE sku=? FOR UPDATE")){
                    p.setString(1,r.sku());try(var rs=p.executeQuery()){if(rs.next())available=rs.getInt(1);}
                }
                status=available>=r.quantity()?"RESERVED":"REJECTED";
                if(status.equals("RESERVED")){
                    try(var p=c.prepareStatement("UPDATE inventory.stock SET available=available-? WHERE sku=?")){p.setInt(1,r.quantity());p.setString(2,r.sku());p.executeUpdate();}
                }
                try(var p=c.prepareStatement("INSERT INTO inventory.reservation(order_id,sku,quantity,status) VALUES(?,?,?,?)")){
                    p.setString(1,r.orderId());p.setString(2,r.sku());p.setInt(3,r.quantity());p.setString(4,status);p.executeUpdate();
                }
                c.commit();return Map.of("status",status);
            }catch(Exception e){c.rollback();throw e;}
        }
    }
    private String existing(Connection c,Request r)throws SQLException{
        try(var p=c.prepareStatement("SELECT sku,quantity,status FROM inventory.reservation WHERE order_id=? FOR UPDATE")){
            p.setString(1,r.orderId());try(var rs=p.executeQuery()){
                if(!rs.next())return null;
                if(!r.sku().equals(rs.getString(1))||r.quantity()!=rs.getInt(2))throw new ResponseStatusException(HttpStatus.CONFLICT,"Reservation mismatch");
                return rs.getString(3);
            }
        }
    }
    @PostMapping("/release")
    public synchronized Map<String,String> release(@RequestBody Request r)throws SQLException{
        validate(r);
        try(var c=connect()){
            c.setAutoCommit(false);
            try{
                String status=existing(c,r);
                if(status==null||status.equals("REJECTED"))throw new ResponseStatusException(HttpStatus.CONFLICT,"No reservation to release");
                if(status.equals("RESERVED")){
                    try(var p=c.prepareStatement("UPDATE inventory.stock SET available=available+? WHERE sku=?")){p.setInt(1,r.quantity());p.setString(2,r.sku());p.executeUpdate();}
                    try(var p=c.prepareStatement("UPDATE inventory.reservation SET status='RELEASED' WHERE order_id=?")){p.setString(1,r.orderId());p.executeUpdate();}
                }
                c.commit();return Map.of("status","RELEASED");
            }catch(Exception e){c.rollback();throw e;}
        }
    }
    @GetMapping("/stock/{sku}")
    public Map<String,Object> stock(@PathVariable String sku)throws SQLException{
        try(var c=connect();var p=c.prepareStatement("SELECT available FROM inventory.stock WHERE sku=?")){
            p.setString(1,sku);try(var rs=p.executeQuery()){
                if(!rs.next())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Unknown SKU");
                return Map.of("sku",sku,"available",rs.getInt(1));
            }
        }
    }
}
