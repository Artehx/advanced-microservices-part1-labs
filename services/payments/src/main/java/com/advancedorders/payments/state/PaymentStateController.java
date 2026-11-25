package com.advancedorders.payments.state;

import java.sql.*;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/payments/training/state")
@ConditionalOnProperty(name="training.state.enabled", havingValue="true")
public class PaymentStateController {
    private final String db,user,password;
    public PaymentStateController(@Value("${training.state.db}") String db,@Value("${training.state.user}") String user,@Value("${training.state.password}") String password){
        this.db=db;this.user=user;this.password=password;
    }
    public record Request(String orderId,boolean decline){}
    @PostMapping("/decide")
    public Map<String,String> decide(@RequestBody Request r)throws SQLException{
        if(r.orderId()==null||r.orderId().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid order");
        try(var c=DriverManager.getConnection(db,user,password)){
            c.setAutoCommit(false);
            try{
                try(var p=c.prepareStatement("INSERT INTO payments.decision(order_id,approved) VALUES (?,?) ON CONFLICT DO NOTHING")){
                    p.setString(1,r.orderId());p.setBoolean(2,!r.decline());p.executeUpdate();
                }
                boolean approved;
                try(var p=c.prepareStatement("SELECT approved FROM payments.decision WHERE order_id=?")){
                    p.setString(1,r.orderId());try(var rs=p.executeQuery()){rs.next();approved=rs.getBoolean(1);}
                }
                if(approved==r.decline())throw new ResponseStatusException(HttpStatus.CONFLICT,"Payment decision mismatch");
                c.commit();return Map.of("status",approved?"APPROVED":"DECLINED");
            }catch(Exception e){c.rollback();throw e;}
        }
    }
}
