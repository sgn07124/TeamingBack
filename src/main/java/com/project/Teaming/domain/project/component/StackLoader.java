package com.project.Teaming.domain.project.component;

import com.project.Teaming.domain.project.entity.Stack;
import com.project.Teaming.domain.project.repository.StackRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StackLoader implements CommandLineRunner {

    private final StackRepository stackRepository;

    public StackLoader(StackRepository stackRepository) {
        this.stackRepository = stackRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 기술 스택 데이터 생성
        List<Stack> stacks = Arrays.asList(
                new Stack("JavaScript"),
                new Stack("TypeScript"),
                new Stack("React"),
                new Stack("Vue"),
                new Stack("Nodejs"),
                new Stack("Spring"),
                new Stack("Java"),
                new Stack("Nextjs"),
                new Stack("Nestjs"),
                new Stack("Express"),
                new Stack("Go"),
                new Stack("C"),
                new Stack("Python"),
                new Stack("Django"),
                new Stack("Swift"),
                new Stack("Kotlin"),
                new Stack("MySql"),
                new Stack("MongoDB"),
                new Stack("php"),
                new Stack("GraphQL"),
                new Stack("FireBase"),
                new Stack("ReactNative"),
                new Stack("Unity"),
                new Stack("Flutter"),
                new Stack("AWS"),
                new Stack("Kubernetes"),
                new Stack("Docker"),
                new Stack("Git"),
                new Stack("Figma"),
                new Stack("Zeplin")
        );

        if (stackRepository.count() == 0) {
            stackRepository.saveAll(stacks);
        }
    }
}
