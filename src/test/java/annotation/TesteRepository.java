package annotation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TesteRepository extends JpaRepository<Test, Long> {

	Test findByNome(String nome);
}
