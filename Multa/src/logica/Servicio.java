package logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.Multa;
import model.Respuesta;

public class Servicio {
    
    public ArrayList<Multa> getMultas() {
        ArrayList<Multa> lstMultas = new ArrayList<>();
        try {
            Connection con = Conexion.startConeccion();
            Statement statement = con.createStatement();
            String query = "SELECT * FROM multa ORDER BY monto DESC";
            ResultSet rs = statement.executeQuery(query);
            
            Multa objMulta;
            while(rs.next()) { // Se ejecuta la misma cantidad de veces que filas tiene la tabla
                objMulta = new Multa();
                int idMulta      = rs.getInt("id_multa");
                String dni       = rs.getString("dni");
                String tipoMulta = rs.getString("tipo_multa");
                Double multa     = rs.getDouble("monto");
                String correo    = rs.getString("correo");
                int punto        = rs.getInt("puntos");
               
                       
                objMulta.setIdMulta(idMulta);
                objMulta.setDni(dni);
                objMulta.setMulta(tipoMulta);
                objMulta.setMonto(multa);
                objMulta.setCorreo(correo);
                objMulta.setPunto(punto);

                lstMultas.add(objMulta);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lstMultas;
    }
    
    public Respuesta validar(Multa multa) {
        Respuesta rpta = new Respuesta();
        rpta.setCodigo(0);
       
        
        if(multa.getMonto() > 1000) {
            rpta.setCodigo(1);
            rpta.setMsj("La multa no puede ser mayor a 1000 soles");
            return rpta;
        }
        if(multa.getMulta().equalsIgnoreCase("Pico placa") && multa.getMonto() < 500 ) {
            rpta.setCodigo(2);
            rpta.setMsj("La multa para pico y placa no puede ser menor a 500");
           return rpta;
        }

        
         if(multa.getMulta().equalsIgnoreCase("Alta velocidad") && multa.getMonto()<400 | multa.getMonto()>570 ){
            rpta.setCodigo(3);
            rpta.setMsj("La cantidad no debe ser menor a 400 ni mayor a 570");   
             return rpta;}
         
         if(multa.getMulta().equalsIgnoreCase("Luz roja") && multa.getMonto()<130 | multa.getMonto()>250){
            rpta.setCodigo(4);
            rpta.setMsj("La cantidad no debe ser menor a 130 ni mayor a 250");           
           return rpta;}
         
         if(multa.getMulta().equalsIgnoreCase("mal estacionado") && multa.getMonto()<100 | multa.getMonto()>190){
            rpta.setCodigo(5);
            rpta.setMsj("La cantidad no debe ser menor a 100 ni mayor a 190");   
             return rpta;}
         if(multa.getMulta().equalsIgnoreCase("Pico placa") && multa.getMonto()<130 | multa.getMonto()>330){
            rpta.setCodigo(6);
            rpta.setMsj("La cantidad no debe ser menor a 130 ni mayor a 330");
         return rpta;}
        
        return rpta;
    }
    
    public Respuesta insertarMulta(Multa multa) {
        Respuesta rpta = new Respuesta();
        try {
            rpta = validar(multa);
            if(rpta.getCodigo() != 0) {
                return rpta;
            }
            int cantidadMultas = getCantidadMultasByDNI_Fecha(multa.getDni(), multa.getFecha());
            if(cantidadMultas == -1) {
                rpta.setCodigo(-1);
                rpta.setMsj("Hubo un error al contabilizar las multas");
                return rpta;
            }
            System.err.println("cantidadMultas::: "+cantidadMultas);
            if(cantidadMultas >= 2) {
                rpta.setCodigo(3);
                rpta.setMsj("No se puede registrar mas de 2 multas por día");
                return rpta;
            }
            int cantidadPuntos = getvalidarcantidad(multa.getDni());
            if(cantidadPuntos+multa.getPunto() > 100) {
                rpta.setCodigo(-1);
                rpta.setMsj("Con "+multa.getPunto()+" puntos , supera los 100 puntos maximos");
                return rpta;
            }
            
            Connection con = Conexion.startConeccion();
            String query = "INSERT INTO `sat`.`multa` (`dni`, `tipo_multa`, `monto`, `correo`, `puntos`, `fec_regi`) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, multa.getDni());
            ps.setString(2, multa.getMulta());
            ps.setDouble(3, multa.getMonto());
            ps.setString(4, multa.getCorreo());
            ps.setInt(5, multa.getPunto());
            ps.setDate(6, new java.sql.Date(multa.getFecha().getTime()) );
            ps.executeUpdate();
            rpta.setCodigo(0); // 0 = no error
            rpta.setMsj("Se registró la multa.");
        } catch (Exception e) {
            e.printStackTrace();
            rpta.setCodigo(-1);
            rpta.setMsj("Hubo un error al registrar la multa.");
        }
        return rpta;
    }
    
    public Respuesta actualizarMulta(Multa multa) {
        Respuesta rpta = new Respuesta();
        try {
            rpta = validar(multa);
            if(rpta.getCodigo() != 0) {
                return rpta;
            }
            Connection con = Conexion.startConeccion();
            String query = "UPDATE `sat`.`multa` SET tipo_multa = ?, monto = ?, correo = ?, puntos = ? WHERE id_multa = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, multa.getMulta());
            ps.setDouble(2, multa.getMonto());
            ps.setString(3, multa.getCorreo());
            ps.setInt(4, multa.getPunto());
            ps.setInt(5, multa.getIdMulta());
            ps.executeUpdate();
            rpta.setCodigo(0); // 0 = no error
            rpta.setMsj("Se actualizó la multa.");
        } catch (Exception e) {
            e.printStackTrace();
            rpta.setCodigo(-1);
            rpta.setMsj("Hubo un error al actualizar la multa.");
        }
        return rpta;
    }
            
    public Respuesta borrarMulta(int idMulta) {
        Respuesta rpta = new Respuesta();
        try {
            Connection con = Conexion.startConeccion();
            String query = "DELETE FROM `sat`.`multa` WHERE id_multa = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, idMulta);
            ps.executeUpdate();
            rpta.setCodigo(0); // 0 = no error
            rpta.setMsj("Se borró la multa.");
        } catch (Exception e) {
            e.printStackTrace();
            rpta.setCodigo(-1);
            rpta.setMsj("Hubo un error al borrar la multa.");
        }
        return rpta;
    }
    
    public int getCantidadMultasByDNI_Fecha(String dni, Date fecha) {
        try {
            //
            Connection con = Conexion.startConeccion();
            String query = "SELECT COUNT(1) AS cantidad FROM `multa` WHERE dni = ? AND DATE(fec_regi) = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, dni);
            ps.setDate(2, new java.sql.Date(fecha.getTime()));
            System.out.println("QUERY A EJECUTAR: "+ps);
            ResultSet rs = ps.executeQuery();
            int cantidadMultas = -1;
            while(rs.next()) {
                cantidadMultas = rs.getInt("cantidad");
            }
            return cantidadMultas;
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
     public int getvalidarcantidad(String dni) {
        try {
            //
            Connection con = Conexion.startConeccion();
            String query = "SELECT COUNT(1),puntos AS cantidad FROM multa WHERE dni = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, dni);
            ResultSet rs = ps.executeQuery();
              int   cantidadPuntos=-2;
            while(rs.next()) {
              cantidadPuntos = rs.getInt("cantidad");
            }
            return cantidadPuntos;
        } catch(Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}