# Copyright 2009 Google Inc. All Rights Reserved.
#
# @author (elsigh@google.com) Lindsey Simon
#
# Instructions from google3:
# blaze build experimental/webdev_course/bundle_fs
# appcfg.py update blaze-bin/experimental/webdev_course/bundle


genrule(name = 'webdev_course.zip',
        srcs = [],
        outs = [ 'webdev-course.zip' ],
        message = 'Building the webdev course ZIP file...',
        cmd = 'OUTZIP="$${PWD}/$@" && cd experimental && ' +
              'find webdev_course -type d | xargs chmod a+rwx &&' +
              'find webdev_course -type f | xargs chmod a+rw && ' +
              'zip -r $$OUTZIP webdev_course')

# AppEngine target.
Fileset(name='bundle_fs',
        entries=[FilesetEntry(files=['app.yaml',
                                     'index.yaml',
                                     'index.html',
                                     'webdev-course.zip']),
                 FilesetEntry(srcdir='demos', destdir='demos'),
                 FilesetEntry(srcdir='exercises', destdir='exercises'),
                 FilesetEntry(srcdir='utilities', destdir='utilities'),],
        out='bundle')

