package eu.europa.ec.sante.ehdsi.openncp.gateway.service;

import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.model.Role;
import eu.europa.ec.sante.ehdsi.openncp.gateway.persistence.repository.RoleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional()
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getRoles() {
        return roleRepository.findAll(Sort.by("name"));
    }

    public Role save(Role role) {
        Role ret = roleRepository.save(role);
        roleRepository.flush();
        return ret;
    }

    public boolean deleteRole(Role role) {
        roleRepository.delete(role);
        roleRepository.flush();
        return true;
    }
}
