package controlador;
import model.plan;

/**
 *
 * @author EUMAPE
 */
public class crud_planes {
    static final int ELEMENTOS = 10;
    public static plan[] planes = new plan[ELEMENTOS];
    public static int contadorPlanes = 0;
    public static int contadorId = 1;

    // para crear o agg un nuevo plan
    public boolean crearPlanes(String nombrePlan, String descripcionPlan, String fechaActivacion, String fechaVencimiento, String estado, double valorMensual, String metodoPago) {
        if (contadorPlanes >= ELEMENTOS) {
            return false;  
        }
        int idGenerado = contadorId++;
        plan nuevoPlan = new plan(idGenerado, nombrePlan, descripcionPlan, fechaActivacion, fechaVencimiento, estado, valorMensual, metodoPago);
        planes[contadorPlanes] = nuevoPlan;
        contadorPlanes++;
        return true;
    }

    // buscar los planes
    public static plan buscarPlanes (int idPlan) {
        for (int i = 0; i < contadorPlanes; i++) {
            if(planes[i].getIdPlan()== idPlan){
                return planes[i];
        }
    }
        return null;
}    
    // para actualizar todo
    public static boolean actualizarPlan (int id, String nombrePlan, String descripcionPlan, String fechaActivacion, String fechaVencimiento, String estado, double valorMensual, String metodoPago) {
        for (int i = 0; i < contadorPlanes; i++) {
       if (planes[i].getIdPlan() == id) {
                planes[i].setNombrePlan(nombrePlan);
                planes[i].setDescripcionPlan(descripcionPlan);
                planes[i].setFechaActivacion(fechaActivacion);
                planes[i].setFechaVencimiento(fechaVencimiento);
                planes[i].setEstado(estado);
                planes[i].setValorMensual(valorMensual);
                planes[i].setMetodoPago(metodoPago);
                return true;
            }
        }
        return false;
    }
    
    //para eliminar el plan
   public static boolean eliminarPlan(int id) {
        for (int i = 0; i < contadorPlanes; i++) {
            if (planes[i].getIdPlan() == id) {
                for (int j = i; j < contadorPlanes - 1; j++) {
                    planes[j] = planes[j + 1];
                }
                planes[contadorPlanes - 1] = null;
                contadorPlanes--;
                return true;
            }
        }
        return false;
    }
}
    
    

