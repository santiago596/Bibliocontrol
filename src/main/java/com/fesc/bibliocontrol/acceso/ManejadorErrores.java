package com.fesc.bibliocontrol.acceso;

import com.fesc.bibliocontrol.dto.ErrorResponse;
import com.fesc.bibliocontrol.negocio.RecursoNoEncontradoException;
import com.fesc.bibliocontrol.negocio.ReglaNegocioException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Acceso al negocio: traduce los errores de validación y de negocio a respuestas HTTP
 * con un formato único que la presentación puede mostrar al usuario.
 */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos.", detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoIlegible(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "El formato de la solicitud no es válido.", List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return responder(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' no es válido.", List.of());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaIncumplida(ReglaNegocioException ex) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), List.of());
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> conflicto(RuntimeException ex) {
        return responder(HttpStatus.CONFLICT,
                "Otra operación modificó el mismo registro al mismo tiempo. Intente nuevamente.", List.of());
    }

    private static ResponseEntity<ErrorResponse> responder(HttpStatus estado, String mensaje, List<String> detalles) {
        return ResponseEntity.status(estado).body(ErrorResponse.de(estado.value(), mensaje, detalles));
    }
}
