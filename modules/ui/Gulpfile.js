var gulp = require('gulp');

var browserify = require('browserify');
var source = require('vinyl-source-stream');
var babelify = require('babelify');
var jest = require('jest-cli');
gulp.task('jest', function(done) {
  var rootDir = './src';
  jest.runCLI({config: {
    'rootDir': rootDir,
    'scriptPreprocessor': '../node_modules/babel-jest',
    'testFileExtensions': ['es6', 'js'],
    'moduleFileExtensions': ['js', 'json', 'es6']
  }}, rootDir, function(success) {
    done(success ? null : 'jest failed');
    process.on('exit', function() {
      process.exit(success ? 0 : 1);
    });
  });
});

gulp.task('js', function () {
    return browserify('./src/ui.js')
        .transform(babelify)
        .bundle()
        .pipe(source('ui.js'))
        .pipe(gulp.dest('./'))
});


gulp.task('default', ['jest', 'js']);