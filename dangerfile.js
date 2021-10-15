const { danger, fail, warn } = require('danger');
const lint = require('@commitlint/lint').default;

const defaultRules = {
    'body-leading-blank': [1, 'always'],
    'header-max-length': [2, 'always', 100],
    'type-case': [2, 'always', 'lower-case'],
    'footer-leading-blank': [1, 'always'],
    'subject-empty': [2, 'never'],
    'subject-case': [
        2,
        'never',
        ['sentence-case', 'start-case', 'pascal-case', 'upper-case'],
    ],
    'type-enum': [
        2,
        'always',
        [
            'build',
            'chore',
            'ci',
            'docs',
            'feat',
            'fix',
            'perf',
            'refactor',
            'revert',
            'style',
            'test',
        ],
    ]
}

const violationsDescriptions = {
    'body-leading-blank': 'Commit message body should be preceded by a blank line',
    'header-max-length': 'Commit header length should not exceed 100 characters',
    'type-case': 'Commit type must be lower-case',
    'footer-leading-blank': 'Commit footer should be preceded by a blank line',
    'subject-empty': 'Commit description must be provided',
    'subject-case': 'Commit description must not be written in the following styles: ' +
        '`sentence-case` -> `Some message`, `start-case` -> `Some Message`, `pascal-case` -> `SomeMessage`, `upper-case` -> `SOMEMESSAGE`',
    'type-enum': 'Invalid commit type. Valid types are as follows: ' +
        '`build`, `chore`, `ci`, `docs`, `feat`, `fix`, `perf`, `refactor`, `revert`, `style`, `test`',
}

const body = danger.github.pr.body;

if(body === null) {
    fail('PR needs to have description!')
}

const report = await lint(body, defaultRules)

report.warnings.forEach((warning) => {
    warn(violationsDescriptions[warning.name])
});

report.errors.forEach((error) => {
    fail(violationsDescriptions[error.name])
});
