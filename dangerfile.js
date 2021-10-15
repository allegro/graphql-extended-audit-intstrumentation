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

const ERROR_HEADER = "❌ FAILURE. Found commit rules violations! Errors:"
const body = danger.github.pr.body;

if(body === null) {
    console.log(`${ERROR_HEADER}\n❌ ERROR: PR needs to have description!`)
    fail(`PR description needed`)
} else {
    lint(body, defaultRules).then((report) => {
        if (report.warnings.length !== 0) {
            console.log("❗ Warnings:")
            report.warnings.forEach((warning) => {
                console.log(`❗ ${violationsDescriptions[warning.name]}`)
            });
        }

        if (report.errors.length !== 0) {
            console.log(ERROR_HEADER)
            report.errors.forEach((error) => {
                console.log(`❌ ${violationsDescriptions[error.name]}`)
            });
            fail(`Errors found`)
        }
    });
}
