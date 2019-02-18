package annotation;

import java.lang.Long;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

class TestService {
  @Autowired
  private TestRepository repository;

  public ResponseEntity save(TestEntity entity) {
    return ResponseEntity.ok(this.repository.save(entity));
  }

  public ResponseEntity one(Long id) {
    return ResponseEntity.ok(this.repository.findById(id));
  }

  public ResponseEntity all() {
    return ResponseEntity.ok(this.repository.findAll());
  }

  public ResponseEntity delete(Long id) {
    this.repository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
