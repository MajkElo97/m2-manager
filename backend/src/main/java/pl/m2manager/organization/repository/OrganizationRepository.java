package pl.m2manager.organization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.m2manager.organization.entity.Organization;

import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}
