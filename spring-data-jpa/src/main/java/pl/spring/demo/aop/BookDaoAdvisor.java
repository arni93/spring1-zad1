package pl.spring.demo.aop;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.beans.factory.annotation.Autowired;

import pl.spring.demo.annotation.NullableId;
import pl.spring.demo.common.Sequence;
import pl.spring.demo.dao.BookDao;
import pl.spring.demo.exception.BookNotNullIdException;
import pl.spring.demo.to.BookTo;
import pl.spring.demo.to.IdAware;

public class BookDaoAdvisor implements MethodBeforeAdvice {
	@Autowired
	private Sequence sequence;

	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {

		if (hasAnnotation(method, target, NullableId.class)) {
			checkNotNullId(args[0]);
			if ((args[0] instanceof BookTo) && (target instanceof BookDao)) {
				BookTo book = (BookTo) args[0];
				if (book.getId() == null) {
					BookDao bookDao = (BookDao) target;
					List<BookTo> allBooks = bookDao.findAll();
					long nextId = sequence.nextValue(allBooks);
					book.setId(nextId);
				}
			}
		}
	}

	private void checkNotNullId(Object o) {
		if (o instanceof IdAware && ((IdAware) o).getId() != null) {
			throw new BookNotNullIdException();
		}
	}

	private boolean hasAnnotation(Method method, Object o, Class annotationClazz) throws NoSuchMethodException {
		boolean hasAnnotation = method.getAnnotation(annotationClazz) != null;

		if (!hasAnnotation && o != null) {
			hasAnnotation = o.getClass().getMethod(method.getName(), method.getParameterTypes())
					.getAnnotation(annotationClazz) != null;
		}
		return hasAnnotation;
	}
}
