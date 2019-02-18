package annotation;

import java.lang.Long;
import org.springframework.data.jpa.repository.JpaRepository;

interface TestEntityRepository extends JpaRepository<TestEntity, Long> {
}
