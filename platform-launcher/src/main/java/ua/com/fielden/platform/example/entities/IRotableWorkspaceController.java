package ua.com.fielden.platform.example.entities;

import java.util.List;

public interface IRotableWorkspaceController {
    @SuppressWarnings("unchecked")
    List<Rotable> save(RotableWorkspace rotableWorkspace);
}
