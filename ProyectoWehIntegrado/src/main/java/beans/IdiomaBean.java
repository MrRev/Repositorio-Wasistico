package beans;

import java.io.Serializable;
import java.util.Locale;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;

@Named("idiomaBean")
@SessionScoped
public class IdiomaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public void cambiar(String idioma) {
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(idioma));
    }
}
