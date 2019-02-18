package annotation;

import java.lang.Long;
import org.springframework.data.jpa.repository.JpaRepository;

interface TestRepository extends JpaRepository<TestEntity, Long> {
}
