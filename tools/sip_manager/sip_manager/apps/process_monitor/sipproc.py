"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)
"""

import os

from django.conf import settings

import models


# thinks its a teeny bit faster to extract the setting once...
SIP_LOG_FILE = settings.SIP_LOG_FILE



class SipProcessException(Exception):
    pass


class SipProcess(object):
    """
    This is the baseclass for sip processes

    each subclass should define a run() that does the actual work

    all locking to the database are done by this baseclass
    """
    SHORT_DESCRIPTION = '' # a one-two word description.

    # For loadbalancing, set to True if this plugin uses a lot of system resources
    # taskmanager will try to spread load depending on what is indicated here
    PLUGIN_TAXES_CPU = False
    PLUGIN_TAXES_DISK_IO = False
    PLUGIN_TAXES_NET_IO = False

    def __init__(self, debug_lvl=2, run_once=False):
        self.debug_lvl = debug_lvl
        self.run_once = run_once # if true plugin should exit after one runthrough
        self.pid = os.getpid()

    def run(self, *args, **kwargs):
        print 'running ', self.short_name()
        return self.run_it(*args, **kwargs)

    def log(self, msg, lvl=1):
        if self.debug_lvl < lvl:
            return
        print msg
        #f = open(LOG_FILE,'a')
        #f.write('%s\n' % msg)
        #f.close()
        open(SIP_LOG_FILE,'a').write('%s\n' % msg)

    def error_log(self, msg):
        print self.short_name(), msg


    def abort_process(self, msg):
        "Terminats process, trying to clean up and remove all pid locks."
        pms = models.ProcessMonitoring.objects.filter(pid=self.pid)
        for pm in pms:
            # TODO: a process failed, flag it, and remove its lock
            pass
        raise SipProcessException(msg)

    # Pid locking mechanisms
    def grab_item(self, cls, pk, task_description):
        "Locks item to current pid, if successfull, returns updated item, otherwise returns None."
        item = cls.objects.filter(pk=pk)[0]
        if not item.pid:
            item.pid = self.pid
            item.save()
            pm = models.ProcessMonitoring(pid=item.pid,
                                          task=task_description)
            pm.save()
            return item, pm
        else:
            return None, None

    # End of Pid locking


    def __unicode__(self):
        return '%s - [%s] %s' % (self.aggregator_id, self.name_code, self.name)

    def short_name(self):
        "Short oneword version of process name."
        # find name of this (sub-) class
        return self.__class__.__name__

    def short_descr(self):
        if not self.SHORT_DESCRIPTION:
            raise NotImplemented('SHORT_DESCRIPTION must be specified in subclass')
        return self.SHORT_DESCRIPTION

