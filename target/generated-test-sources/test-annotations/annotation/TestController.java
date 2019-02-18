package annotation;

import java.lang.Long;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/test")
class TestController {
  @Autowired
  private TestService service;

  @GetMapping("{id}")
  public ResponseEntity one(@PathVariable("id") Long id) {
    return this.service.one(id);
  }

  @GetMapping
  public ResponseEntity all() {
    return this.service.all();
  }

  @PostMapping
  public ResponseEntity save(@RequestBody TestEntity entity) {
    return this.service.save(entity);
  }

  @DeleteMapping("{id}")
  public ResponseEntity delete(@PathVariable("id") Long id) {
    return this.service.delete(id);
  }
}
