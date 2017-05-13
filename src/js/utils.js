export function uriToPath(uri) {
   return '/' + uri.substr(uri.split(':')[0].length + 1).split(':').join('/')
}