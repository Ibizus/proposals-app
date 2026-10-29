package org.propflow.back.util;

import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class UtilLazy {

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T> void initializeLazyOneToManyByJoinFetch(
      EntityManager entityManager,
      Class classOne,
      Class classMany,
      Long oneId,
      Consumer<Set<T>> consumer) {
    var many =
        entityManager
            .createQuery(
                "select many "
                    + "from "
                    + classMany.getSimpleName()
                    + " many "
                    + "join fetch many."
                    + classOne.getSimpleName().toLowerCase()
                    + " "
                    + "where many."
                    + classOne.getSimpleName().toLowerCase()
                    + ".id  = :id",
                classMany)
            .setParameter("id", oneId)
            .getResultList();
    var manySet = new HashSet<>(many);
    consumer.accept(manySet);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static <T> void initializeLazyManyToManyByJoinFetch(
      EntityManager entityManager,
      Class classManyFrom,
      Class classManyJoinFetch,
      Long manyJoinFetchId,
      Consumer<Set<T>> consumer) {
    var many =
        entityManager
            .createQuery(
                "select many "
                    + "from "
                    + classManyFrom.getSimpleName()
                    + " many "
                    + "join fetch many."
                    + classManyJoinFetch.getSimpleName().toLowerCase()
                    + "s manyjf "
                    + "where manyjf.id  = :id",
                classManyFrom)
            .setParameter("id", manyJoinFetchId)
            .getResultList();
    var manySet = new HashSet<>(many);
    consumer.accept(manySet);
  }
}
