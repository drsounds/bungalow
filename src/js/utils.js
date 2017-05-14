export function uriToPath(uri) {
    if (uri == null)
        return '/';
    
   return '/' + uri.substr(uri.split(':')[0].length + 1).split(':').join('/')
}